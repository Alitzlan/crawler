package crawler.dht;

import crawler.common.NodeInfo;
import crawler.common.UrlInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import static crawler.dht.Chord.FINGER_TABLE_SIZE;

/*
 * The object that provides node rpc functions
 * Created by Chi
 */

public class ChordNode extends NodeInfo implements ChordRPC {
    private class Finger {
        int start;
        IntRange range;
        ChordNode node;
    }

    private Finger[] finger_table = new Finger[FINGER_TABLE_SIZE];
    private Hashtable<Integer, UrlInfo> hashtable;
    private ChordNode predecessor;

    public ChordNode() {

    }

    public ChordNode find_successor(int id) throws RemoteException, NotBoundException {
        ChordNode pred_for_id = find_predecessor(id);
        return pred_for_id.finger_table[0].node;
    }

    public ChordNode find_predecessor(int id) throws RemoteException, NotBoundException {
        ChordNode pred_for_id = this;
        IntRange test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id);
        while (!test_range.containOpenClose(id)) {
            if(pred_for_id.id != this.id) { //RPC
                Registry registry = LocateRegistry.getRegistry(pred_for_id.addr.getHostName());
                ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
                pred_for_id = stub.closest_preceding_finger(id);
            }
            else
                pred_for_id = pred_for_id.closest_preceding_finger(id);
            test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id);
        }
        return pred_for_id;
    }

    public ChordNode closest_preceding_finger(int id) {
        IntRange testrange = new IntRange(this.id, id, Chord.MAX_NUM_OF_NODE);
        for (int i = FINGER_TABLE_SIZE - 1; i >= 0; i--)
            if (testrange.containOpenOpen(finger_table[i].node.id))
                return finger_table[i].node;
        return this;
    }

    public void join(ChordNode n) throws RemoteException, NotBoundException {
        try {
            Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            init_finger_table(n);
            update_others();
            // TODO: move keys responsibility from successor
        }
        catch (java.rmi.ConnectException e) { //no other node exists
            for(int i = 0; i < FINGER_TABLE_SIZE; i++)
                finger_table[i].node = this;
            predecessor = this;
        }
    }

    public void init_finger_table(ChordNode n) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
        ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
        finger_table[0].node = stub.find_predecessor(finger_table[0].start);
        predecessor = finger_table[0].node.predecessor;
        finger_table[0].node.predecessor = this;
        IntRange testrange;
        for(int i = 0; i < FINGER_TABLE_SIZE-1; i++) {
            testrange = new IntRange(this.id, finger_table[i].node.id);
            if(testrange.containCloseOpen(finger_table[i+1].start))
                finger_table[i+1].node = finger_table[i].node;
            else
                finger_table[i+1].node = stub.find_successor(finger_table[i+1].start);
        }
    }

    public void update_others() throws RemoteException, NotBoundException {
        for(int i = 0; i < FINGER_TABLE_SIZE; i++) {
            ChordNode p = find_predecessor(this.id - (int) Math.pow(2, i - 1));
            Registry registry = LocateRegistry.getRegistry(p.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            stub.update_finger_table(this, i);
        }
    }

    public void update_finger_table(ChordNode s, int i) throws RemoteException, NotBoundException {
        IntRange testrange = new IntRange(this.id, finger_table[i].node.id);
        if(testrange.containCloseOpen(s.id)) {
            finger_table[i].node = s;
            ChordNode p = predecessor;
            Registry registry = LocateRegistry.getRegistry(p.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            stub.update_finger_table(s,i);
        }
    }

    public void printFingerTable() {
        Logger logger = LoggerFactory.getLogger(ChordNode.class);
        logger.info(String.format("start\tinterval\tsuccessor"));
        for (Finger finger : finger_table) {
            logger.info(String.format("%d\t[%d, %d)\t%d", finger.start, finger.range.getMin(), finger.range.getMax(), finger.node.id));
        }
    }

    public static void main(String args[]) {

        try {
            ChordNode obj = new ChordNode();
            ChordNode stub = (ChordNode) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("ChordNode", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

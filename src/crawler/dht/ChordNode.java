package crawler.dht;

import crawler.common.NodeInfo;
import crawler.common.UrlInfo;
import crawler.test.TestPolymophism.ParentClass;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static crawler.dht.ChordPolicy.FINGER_TABLE_SIZE;
import static crawler.dht.ChordPolicy.MAX_NUM_OF_NODE;
import static crawler.dht.ChordPolicy.SHA1_SUBSTR_BEGIN;

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

    private Finger[] finger_table;
    private HashMap<String, UrlInfo> hashtable;
    private ChordNode predecessor;
    public static Logger logger;

    public ChordNode() throws RemoteException {
        logger = LoggerFactory.getLogger(ChordNode.class);
        logger.info("Node Created");
        finger_table = new Finger[FINGER_TABLE_SIZE];
        hashtable = new HashMap<String, UrlInfo>();
        for(int i = 0;i < FINGER_TABLE_SIZE;i++)
            finger_table[i] = new Finger();
    }

    public ChordNode find_successor(int id) throws RemoteException, NotBoundException {
        logger.debug("Finding successor for " + id);
        ChordNode pred_for_id = find_predecessor(id);
        return pred_for_id.finger_table[0].node;
    }

    public ChordNode find_predecessor(int id) throws RemoteException, NotBoundException {
        logger.debug("Finding predecessor for " + id);
        ChordNode pred_for_id = this;
        IntRange test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id, MAX_NUM_OF_NODE);
        while (!test_range.containOpenClose(id)) {
            if (pred_for_id.id != this.id) { //RPC
                Registry registry = LocateRegistry.getRegistry(pred_for_id.addr.getHostName());
                ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
                pred_for_id = stub.closest_preceding_finger(id);
            } else
                pred_for_id = pred_for_id.closest_preceding_finger(id);
            test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id, MAX_NUM_OF_NODE);
        }
        return pred_for_id;
    }

    public ChordNode closest_preceding_finger(int id) throws RemoteException, NotBoundException {
        logger.debug("Finding closest preceding finger for " + id);
        IntRange testrange = new IntRange(this.id, id, MAX_NUM_OF_NODE);
        for (int i = finger_table.length - 1; i >= 0; i--)
            if (testrange.containOpenOpen(finger_table[i].node.id))
                return finger_table[i].node;
        return this;
    }

    public void join(ChordNode n) throws RemoteException, NotBoundException {
        try {
            if (n == null)
                throw new java.rmi.ConnectException("Null node info");
            logger.debug(String.format("Joining node %s", n.hostname));
            Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            init_finger_table(n);
            update_others();
            // TODO: move keys responsibility from successor
        } catch (java.rmi.ConnectException e) { //no other node exists
            for (int i = 0; i < FINGER_TABLE_SIZE; i++) {
                finger_table[i].start = (this.id + (int) Math.pow(2, i))%MAX_NUM_OF_NODE;
                finger_table[i].range = new IntRange((this.id + (int)Math.pow(2, i))%MAX_NUM_OF_NODE, (this.id + (int)Math.pow(2, i+1))%MAX_NUM_OF_NODE, MAX_NUM_OF_NODE);
                finger_table[i].node = this;
            }
            predecessor = this;
        }
        printFingerTable();
    }

    public void init_finger_table(ChordNode n) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
        ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
        finger_table[0].node = stub.find_predecessor(finger_table[0].start);
        predecessor = finger_table[0].node.predecessor;
        finger_table[0].node.predecessor = this;
        IntRange testrange;
        for (int i = 0; i < FINGER_TABLE_SIZE - 1; i++) {
            testrange = new IntRange(this.id, finger_table[i].node.id, MAX_NUM_OF_NODE);
            if (testrange.containCloseOpen(finger_table[i + 1].start))
                finger_table[i + 1].node = finger_table[i].node;
            else
                finger_table[i + 1].node = stub.find_successor(finger_table[i + 1].start);
        }
    }

    public void update_others() throws RemoteException, NotBoundException {
        for (int i = 0; i < FINGER_TABLE_SIZE; i++) {
            ChordNode p = find_predecessor(this.id - (int) Math.pow(2, i - 1));
            Registry registry = LocateRegistry.getRegistry(p.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            stub.update_finger_table(this, i);
        }
    }

    public void update_finger_table(ChordNode s, int i) throws RemoteException, NotBoundException {
        IntRange testrange = new IntRange(this.id, finger_table[i].node.id, MAX_NUM_OF_NODE);
        if (testrange.containCloseOpen(s.id)) {
            finger_table[i].node = s;
            ChordNode p = predecessor;
            Registry registry = LocateRegistry.getRegistry(p.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            stub.update_finger_table(s, i);
        }
    }

    public UrlInfo lookup(String url) throws RemoteException, NotBoundException {
        String sha1 = DigestUtils.sha1Hex("www.google.com.hk");
        long testlong = Long.parseUnsignedLong(sha1.substring(SHA1_SUBSTR_BEGIN), 16);
        int keyid = (int) (testlong % MAX_NUM_OF_NODE);
        ChordNode node = find_successor(keyid);
        if (node.id == this.id) {
            return lookup_local(url);
        } else {
            Registry registry = LocateRegistry.getRegistry(node.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            return stub.lookup_local(url);
        }
    }

    public UrlInfo lookup_local(String url) throws RemoteException, NotBoundException {
        if (hashtable.containsKey(url))
            return hashtable.get(url);
        else
            return null;
    }

    public boolean insert(String url) throws RemoteException, NotBoundException {
        logger.debug("Attempting insertion of " + url);
        String sha1 = DigestUtils.sha1Hex(url);
        long testlong = Long.parseUnsignedLong(sha1.substring(SHA1_SUBSTR_BEGIN), 16);
        int keyid = (int) (testlong % MAX_NUM_OF_NODE);
        ChordNode node = find_successor(keyid);
        if (node.id == this.id) {
            return insert_local(url);
        } else {
            Registry registry = LocateRegistry.getRegistry(node.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC");
            return stub.insert_local(url);
        }
    }

    public boolean insert_local(String url) throws RemoteException, NotBoundException {
        if (hashtable.containsKey(url))
            return false;
        else {
            UrlInfo newurl = new UrlInfo(url);
            hashtable.put(url, newurl);
            return true;
        }
    }

    public void printFingerTable() {
        logger.info(String.format("start\tinterval\tsuccessor"));
        for (Finger finger : finger_table) {
            logger.info(String.format("%d\t\t[%d, %d)\t\t%d", finger.start, finger.range.getMin(), finger.range.getMax(), finger.node.id));
        }
    }

    public void printHashTable() {
        for (Map.Entry<String, UrlInfo> entry : hashtable.entrySet()) {
            logger.info(String.format("%s\t%s", entry.getKey(), entry.getValue().timestamp.toString()));
        }
    }

    public static void main(String args[]) {

        try {
            ChordNode node = new ChordNode();
            ChordRPC stub = (ChordRPC) UnicastRemoteObject.exportObject(node, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("ChordNode", stub);

            node.join(null);

            stub.insert("www.google.com");
            node.printHashTable();
            logger.info(String.format("www.google.com not exist? %b", stub.insert("www.google.com")));

            logger.info("Server ready");
        } catch (Exception e) {
            logger.error("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

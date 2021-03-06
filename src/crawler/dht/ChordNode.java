package crawler.dht;

import crawler.common.NodeInfo;
import crawler.common.UrlInfo;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.rmi.AlreadyBoundException;
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

public class ChordNode implements ChordRPC {

    public ChordNodeInfo myinfo;
    public HashMap<String, UrlInfo> hashtable;
    public static Logger logger;

    public ChordNode() throws RemoteException, UnknownHostException {
        logger = LoggerFactory.getLogger(ChordNode.class);
        myinfo = new ChordNodeInfo();

        myinfo.id = 0;
        myinfo.hostname = InetAddress.getLocalHost().getHostName();
        myinfo.addr = new InetSocketAddress(myinfo.hostname, 1024);

        myinfo.finger_table = new ChordFinger[FINGER_TABLE_SIZE];
        hashtable = new HashMap<String, UrlInfo>();
        for (int i = 0; i < FINGER_TABLE_SIZE; i++)
            myinfo.finger_table[i] = new ChordFinger();

        logger.info("Node Created");
    }

    public ChordNode(short id, int port) throws RemoteException, UnknownHostException {
        logger = LoggerFactory.getLogger(ChordNode.class);
        myinfo = new ChordNodeInfo();

        myinfo.id = id;
        myinfo.hostname = InetAddress.getLocalHost().getHostName();
        myinfo.addr = new InetSocketAddress(myinfo.hostname, port);

        myinfo.finger_table = new ChordFinger[FINGER_TABLE_SIZE];
        hashtable = new HashMap<String, UrlInfo>();
        for (int i = 0; i < FINGER_TABLE_SIZE; i++)
            myinfo.finger_table[i] = new ChordFinger();

        logger.info(String.format("Node Created for host: %s on port %d", myinfo.hostname, port));
    }

    public ChordNodeInfo find_successor(int id) throws RemoteException, NotBoundException {
        logger.debug("find_successor for " + id);
        ChordNodeInfo pred_for_id = find_predecessor(id);
        return pred_for_id.finger_table[0].node;
    }

    public ChordNodeInfo find_predecessor(int id) throws RemoteException, NotBoundException {
        logger.debug("find_predecessor for " + id);
        ChordNodeInfo pred_for_id = myinfo;
        IntRange test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id, MAX_NUM_OF_NODE);
        while (!test_range.containOpenClose(id)) {
            if (pred_for_id.id != myinfo.id) { //RPC
                logger.debug("Relaying closest_preceding_finger to Node " + pred_for_id.id);
                Registry registry = LocateRegistry.getRegistry(pred_for_id.addr.getHostName());
                ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + pred_for_id.addr.getPort());
                pred_for_id = stub.closest_preceding_finger(id);
            } else {
                pred_for_id = closest_preceding_finger(id);
                if (pred_for_id.id != myinfo.id) {
                    Registry registry = LocateRegistry.getRegistry(pred_for_id.addr.getHostName());
                    ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + pred_for_id.addr.getPort());
                    pred_for_id = stub.get_info();
                }
            }
            test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id, MAX_NUM_OF_NODE);
        }
        pred_for_id = renew_info(pred_for_id);
        logger.debug("find_predecessor for " + id + ", result " + pred_for_id.id);
        return pred_for_id;
    }

    public ChordNodeInfo find_predecessor_close(int id) throws RemoteException, NotBoundException {
        logger.debug("find_predecessor_close for " + id);
        ChordNodeInfo pred_for_id = myinfo;
        IntRange test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id, MAX_NUM_OF_NODE);
        while (!test_range.containOpenClose(id)) {
            if (pred_for_id.id != myinfo.id) { //RPC
                logger.debug("Relaying closest_preceding_finger to Node " + pred_for_id.id);
                Registry registry = LocateRegistry.getRegistry(pred_for_id.addr.getHostName());
                ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + pred_for_id.addr.getPort());
                pred_for_id = stub.closest_preceding_finger(id);
            } else {
                pred_for_id = closest_preceding_finger(id);
                if (pred_for_id.id != myinfo.id) {
                    Registry registry = LocateRegistry.getRegistry(pred_for_id.addr.getHostName());
                    ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + pred_for_id.addr.getPort());
                    pred_for_id = stub.get_info();
                }
            }
            test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].node.id, MAX_NUM_OF_NODE);
        }
        pred_for_id = renew_info(pred_for_id);
        if (id == pred_for_id.finger_table[0].node.id) {
            pred_for_id = pred_for_id.finger_table[0].node;
        }
        logger.debug("find_predecessor_close for " + id + ", result " + pred_for_id.id);
        return pred_for_id;
    }

    public ChordNodeInfo closest_preceding_finger(int id) throws RemoteException, NotBoundException {
        logger.debug("closest_preceding_finger for " + id);
        IntRange testrange = new IntRange(myinfo.id, id, MAX_NUM_OF_NODE);
        for (int i = myinfo.finger_table.length - 1; i >= 0; i--)
            if (testrange.containOpenOpen(myinfo.finger_table[i].node.id)) {
                logger.debug("closest_preceding_finger for " + id + ", result " + myinfo.finger_table[i].node.id);
                return myinfo.finger_table[i].node;
            }
        logger.debug("closest_preceding_finger for " + id + ", result " + myinfo.id);
        return myinfo;
    }

    public void join(ChordNodeInfo n) throws RemoteException, NotBoundException {
        try {
            if (n == null)
                throw new java.rmi.ConnectException("Null node info");
            logger.debug(String.format("Joining node %s:%d", n.hostname, n.addr.getPort()));
            Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + n.addr.getPort());
            init_finger_table(n);
            update_others();
            init_finger_table(n);

            //move keys responsibility (predecessor, self] from successor
            Registry registry2 = LocateRegistry.getRegistry(myinfo.finger_table[0].node.addr.getHostName());
            ChordRPC stub2 = (ChordRPC) registry.lookup("ChordRPC" + myinfo.finger_table[0].node.addr.getPort());
            stub2.transfer_hash_table(myinfo, myinfo.predecessor.id);

        } catch (java.rmi.ConnectException e) { //no other node exists
            logger.warn("joining node is null or not up");
            for (int i = 0; i < FINGER_TABLE_SIZE; i++) {
                myinfo.finger_table[i].start = (myinfo.id + (int) Math.pow(2, i)) % MAX_NUM_OF_NODE;
                myinfo.finger_table[i].range = new IntRange((myinfo.id + (int) Math.pow(2, i)) % MAX_NUM_OF_NODE, (myinfo.id + (int) Math.pow(2, i + 1)) % MAX_NUM_OF_NODE, MAX_NUM_OF_NODE);
                myinfo.finger_table[i].node = myinfo;
            }
            myinfo.predecessor = myinfo;
            myinfo.predecessor = myinfo;
        }
        printFingerTable();
    }

    public void init_finger_table(ChordNodeInfo n) throws RemoteException, NotBoundException {
        logger.debug("init_finger_table with Node " + n.id);
        Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
        ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + n.addr.getPort());
        for (int i = 0; i < FINGER_TABLE_SIZE; i++) {
            myinfo.finger_table[i].start = (myinfo.id + (int) Math.pow(2, i)) % MAX_NUM_OF_NODE;
            myinfo.finger_table[i].range = new IntRange((myinfo.id + (int) Math.pow(2, i)) % MAX_NUM_OF_NODE, (myinfo.id + (int) Math.pow(2, i + 1)) % MAX_NUM_OF_NODE, MAX_NUM_OF_NODE);
        }
        myinfo.finger_table[0].node = stub.find_successor(myinfo.finger_table[0].start);
        if (myinfo.finger_table[0].node.predecessor.id != myinfo.id)
            myinfo.predecessor = myinfo.finger_table[0].node.predecessor;
        myinfo.finger_table[0].node.predecessor = myinfo;
        Registry registry2 = LocateRegistry.getRegistry(myinfo.finger_table[0].node.addr.getHostName());
        ChordRPC stub2 = (ChordRPC) registry.lookup("ChordRPC" + myinfo.finger_table[0].node.addr.getPort());
        stub2.set_predecessor(myinfo);
        IntRange testrange;
        for (int i = 0; i < FINGER_TABLE_SIZE - 1; i++) {
            testrange = new IntRange(myinfo.id, myinfo.finger_table[i].node.id, MAX_NUM_OF_NODE);
            if (testrange.containCloseOpen(myinfo.finger_table[i + 1].start)) {
                myinfo.finger_table[i + 1].node = myinfo.finger_table[i].node;
            } else {
                myinfo.finger_table[i + 1].node = stub.find_successor(myinfo.finger_table[i + 1].start);
            }
        }
    }

    public void update_others() throws RemoteException, NotBoundException {
        logger.debug("update_others");
        for (int i = 0; i < FINGER_TABLE_SIZE; i++) {
            short predid = (short) (myinfo.id - Math.pow(2, i));
            if (predid < 0) predid += MAX_NUM_OF_NODE;
            ChordNodeInfo p = find_predecessor_close(predid);
            if (p.id != myinfo.id) {
                Registry registry = LocateRegistry.getRegistry(p.addr.getHostName());
                ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + p.addr.getPort());
                logger.debug("update_finger_table of node " + p.id + "'s finger " + i);
                stub.update_finger_table(myinfo, i);
                stub.printFingerTable();
            }
        }
    }

    public void update_finger_table(ChordNodeInfo s, int i) throws RemoteException, NotBoundException {
        logger.debug("update_finger_table for node " + s.id + " to finger " + i);
        IntRange testrange = new IntRange(myinfo.id, myinfo.finger_table[i].node.id, MAX_NUM_OF_NODE);
        if (testrange.containCloseOpen(s.id)) {
            myinfo.finger_table[i].node = s;
            ChordNodeInfo p = myinfo.predecessor;
            if (p.id != s.id) {
                logger.debug("update_finger_table of node " + p.id + "'s finger " + i);
                Registry registry = LocateRegistry.getRegistry(p.addr.getHostName());
                ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + p.addr.getPort());
                stub.update_finger_table(s, i);
                stub.printFingerTable();
            }
        }
    }

    public void transfer_hash_table(ChordNodeInfo n, int start) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
        ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + n.addr.getPort());
        IntRange testrange = new IntRange(start, myinfo.id);
        for (Map.Entry<String, UrlInfo> entry : hashtable.entrySet()) {
            String sha1 = DigestUtils.sha1Hex(entry.getKey());
            long testlong = Long.parseUnsignedLong(sha1.substring(SHA1_SUBSTR_BEGIN), 16);
            int keyid = (int) (testlong % MAX_NUM_OF_NODE);
            if (testrange.containOpenClose(keyid)) {
                stub.insert_local(entry.getKey());
                hashtable.remove(entry.getKey());
            }
        }
    }

    public UrlInfo lookup(String url) throws RemoteException, NotBoundException {
        String sha1 = DigestUtils.sha1Hex(url);
        long testlong = Long.parseUnsignedLong(sha1.substring(SHA1_SUBSTR_BEGIN), 16);
        int keyid = (int) (testlong % MAX_NUM_OF_NODE);
        logger.debug("lookup url " + url + ", key:" + keyid);
        ChordNodeInfo node = find_successor(keyid);
        if (node.id == myinfo.id) {
            return lookup_local(url);
        } else {
            logger.debug("relay lookup to node " + node.id);
            Registry registry = LocateRegistry.getRegistry(node.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + node.addr.getPort());
            return stub.lookup_local(url);
        }
    }

    public UrlInfo lookup_local(String url) throws RemoteException, NotBoundException {
        logger.debug("local lookup url " + url);
        if (hashtable.containsKey(url)) {
            logger.debug("url not found");
            return hashtable.get(url);
        } else {
            logger.debug("url exist");
            return null;
        }
    }

    public boolean insert(String url) throws RemoteException, NotBoundException {
        String sha1 = DigestUtils.sha1Hex(url);
        long testlong = Long.parseUnsignedLong(sha1.substring(SHA1_SUBSTR_BEGIN), 16);
        int keyid = (int) (testlong % MAX_NUM_OF_NODE);
        logger.debug("insert url " + url + ", key:" + keyid);
        ChordNodeInfo node = find_successor(keyid);
        if (node.id == myinfo.id) {
            return insert_local(url);
        } else {
            logger.debug("relay insert to node " + node.id);
            Registry registry = LocateRegistry.getRegistry(node.addr.getHostName());
            ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + node.addr.getPort());
            return stub.insert_local(url);
        }
    }

    public boolean insert_local(String url) throws RemoteException, NotBoundException {
        logger.debug("local insert url " + url);
        if (hashtable.containsKey(url)) {
            logger.debug("url exist, abort");
            return false;
        } else {
            logger.debug("url inserted");
            UrlInfo newurl = new UrlInfo(url);
            hashtable.put(url, newurl);
            return true;
        }
    }

    public void set_predecessor(ChordNodeInfo n) throws RemoteException {
        myinfo.predecessor = n;
    }

    public ChordNodeInfo renew_info(ChordNodeInfo n) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(n.addr.getHostName());
        ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC" + n.addr.getPort());
        return stub.get_info();
    }

    public ChordNodeInfo get_info() throws RemoteException {
        return myinfo;
    }

    public void printFingerTable() throws RemoteException {
        logger.info(String.format("start\tinterval\tsuccessor"));
        for (ChordFinger finger : myinfo.finger_table) {
            logger.info(String.format("%d\t\t[%d, %d)\t\t%d", finger.start, finger.range.min, finger.range.max, finger.node.id));
        }
        logger.info("pred: " + myinfo.predecessor.id);
    }

    public void printHashTable() throws RemoteException {
        logger.info(String.format("local hashtable"));
        if(hashtable.entrySet().isEmpty()){
            logger.info(String.format("empty"));
            return;
        }
        for (Map.Entry<String, UrlInfo> entry : hashtable.entrySet()) {
            logger.info(String.format("%s\t%s", entry.getKey(), entry.getValue().timestamp.toString()));
        }
    }

    public static void main(String args[]) throws ParseException, RemoteException, UnknownHostException, AlreadyBoundException {
        Options options = new Options();
        options.addOption("i", "id", true, "assign a node id for current instance");
        options.addOption("p", "port", true, "assign a port number for current instance");
        options.addOption("j", "join", true, "\"-j n\" ask the current instance to join node n");
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        ChordNode node;
        ChordRPC stub;
        Registry registry;

        try {
            node = new ChordNode(Short.valueOf(cmd.getOptionValue("i", "0")), Integer.valueOf(cmd.getOptionValue("p", "1024")));
            stub = (ChordRPC) UnicastRemoteObject.exportObject(node, 0);

            registry = LocateRegistry.getRegistry();

            registry.rebind("ChordRPC" + node.myinfo.addr.getPort(), stub);

            if (cmd.hasOption("j")) {
                ChordNodeInfo leadernode = new ChordNodeInfo(cmd.getOptionValue("j", "localhost:1024"));
                node.join(leadernode);
            } else
                node.join(null);

            logger.info("Server ready");
        } catch (java.rmi.NotBoundException e) {
            node = new ChordNode(Short.valueOf(cmd.getOptionValue("i", "0")), Integer.valueOf(cmd.getOptionValue("p", "1024")));
            stub = (ChordRPC) UnicastRemoteObject.exportObject(node, 0);
            registry = LocateRegistry.getRegistry();
            registry.bind("ChordRPC" + node.myinfo.addr.getPort(), stub);
        } catch (Exception e) {
            logger.error("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

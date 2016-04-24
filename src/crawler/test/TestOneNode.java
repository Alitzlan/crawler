package crawler.test;

import crawler.dht.ChordNode;
import crawler.dht.ChordRPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class TestOneNode {

    public static void main(String args[]) {
        Logger logger = LoggerFactory.getLogger(TestOneNode.class);
        try {
            ChordNode node = new ChordNode();
            ChordRPC stub = (ChordRPC) UnicastRemoteObject.exportObject(node, 1024);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("ChordRPC", stub);

            node.join(null);

            stub.insert("www.google.com");
            stub.insert("www.baidu.com");
            node.printHashTable();
            logger.info(String.format("www.google.com not exist? %b", stub.insert("www.google.com")));

            logger.info("Server ready");
        } catch (Exception e) {
            logger.error("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

package crawler.test.TestChord;

import crawler.dht.ChordNode;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

public class TestNode {
    public static void main(String[] args) throws ParseException, RemoteException, UnknownHostException, AlreadyBoundException {
        Logger logger = LoggerFactory.getLogger(TestNode.class);
        ChordNode.main(args);
    }
}

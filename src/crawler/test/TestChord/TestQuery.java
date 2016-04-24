package crawler.test.TestChord;

import crawler.dht.ChordRPC;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Alitz-YC on 4/24/2016.
 */
public class TestQuery {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC1024");
        System.out.println(stub.insert("www.google.com"));
        System.out.println(stub.insert("www.google.com"));
    }
}

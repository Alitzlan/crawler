package crawler.test.TestChord;

import crawler.dht.ChordRPC;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class TestQueryInteractive {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(args[0]);
        ChordRPC stub = (ChordRPC) registry.lookup("ChordRPC1024");
        Scanner reader = new Scanner(System.in);
        while(true) {
            System.out.println("Input URL to insert:");
            System.out.println(stub.insert(reader.next()));
        }
    }
}

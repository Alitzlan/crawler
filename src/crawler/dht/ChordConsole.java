package crawler.dht;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/*
 * Console for querying nodes
 * Created by Chi
 */

public class ChordConsole {
    public static void main(String[] args) throws RemoteException, NotBoundException, URISyntaxException {
        Registry registry;
        ChordRPC stub = null;
        URI uri;
        String hostname;
        int port;
        if(args.length == 1 && args[0].length() > 0) {
            uri = new URI("rpc://"+args[0]);
            hostname = uri.getHost();
            port = uri.getPort();
            if (port<0)
                port = 1024;
            registry = LocateRegistry.getRegistry(hostname);
            stub = (ChordRPC) registry.lookup("ChordRPC"+port);
        }

        Scanner reader = new Scanner(System.in);
        String cmd;
        while(true) {
            System.out.print("cmd> ");
            cmd = reader.nextLine();
            if(stub == null) {
                System.out.println("Please sethost first!");
                continue;
            }
            if(cmd.length() == 0)
                continue;
            String[] splitcmd = cmd.split(" ");
            switch(splitcmd[0]) {
                case "sethost":
                    uri = new URI("rpc://"+splitcmd[1]);
                    hostname = uri.getHost();
                    port = uri.getPort();
                    if (port<0)
                        port = 1024;
                    registry = LocateRegistry.getRegistry(hostname);
                    stub = (ChordRPC) registry.lookup("ChordRPC"+port);
                    break;
                case "printfinger":
                    stub.printFingerTable();
                    break;
                case "printhash":
                    stub.printHashTable();
                    break;
                case "insert":
                    boolean result = stub.insert(splitcmd[1]);
                    if(result)
                        System.out.println("insert success");
                    else
                        System.out.println("insert failed: url exists");
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
    }
}

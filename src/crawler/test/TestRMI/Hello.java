package crawler.test.TestRMI;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public interface Hello extends Remote {
    Msg sayHello() throws RemoteException;
    Registry getNext() throws RemoteException, UnknownHostException;
}

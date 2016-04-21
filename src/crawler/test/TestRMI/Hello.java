package crawler.test.TestRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Hello extends Remote {
    Msg sayHello() throws RemoteException;
}

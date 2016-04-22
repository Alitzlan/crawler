package crawler.dht;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Remote procedure calls for Chord
 * Created by Chi
 */

public interface ChordRPC extends Remote{
    public ChordNode find_successor(int id) throws RemoteException, NotBoundException;
    public ChordNode find_predecessor(int id) throws RemoteException, NotBoundException;
    public ChordNode closest_preceding_finger(int id);

    public void join(ChordNode n) throws RemoteException, NotBoundException;
    public void init_finger_table(ChordNode n) throws RemoteException, NotBoundException;
    public void update_others() throws RemoteException, NotBoundException;
    public void update_finger_table(ChordNode s, int i) throws RemoteException, NotBoundException;
}

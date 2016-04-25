package crawler.dht;

import crawler.common.UrlInfo;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Remote procedure calls for ChordPolicy
 * Created by Chi
 */

public interface ChordRPC extends Remote{
    public ChordNodeInfo find_successor(int id) throws RemoteException, NotBoundException;
    public ChordNodeInfo find_predecessor(int id) throws RemoteException, NotBoundException;
    public ChordNodeInfo find_predecessor_close(int id) throws RemoteException, NotBoundException;
    public ChordNodeInfo closest_preceding_finger(int id) throws RemoteException, NotBoundException;

    public void init_finger_table(ChordNodeInfo n) throws RemoteException, NotBoundException;
    public void update_others() throws RemoteException, NotBoundException;
    public void update_finger_table(ChordNodeInfo s, int i) throws RemoteException, NotBoundException;

    public UrlInfo lookup(String url) throws RemoteException, NotBoundException;
    public UrlInfo lookup_local(String url) throws RemoteException, NotBoundException;
    public boolean insert(String url) throws RemoteException, NotBoundException;
    public boolean insert_local(String url) throws RemoteException, NotBoundException;

    public void printFingerTable() throws RemoteException;
    public void printHashTable() throws RemoteException;

    public void set_predecessor(ChordNodeInfo n) throws RemoteException;
    public ChordNodeInfo get_info() throws RemoteException;
}

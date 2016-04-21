package crawler.dht;

import java.rmi.Remote;

/*
 * Remote procedure calls for Chord
 * Created by Chi
 */

public interface ChordRPC extends Remote{
    public ChordPeer find_successor(int id);
    public ChordPeer find_predecessor(int id);
    public ChordPeer closest_preceding_finger(int id);
}

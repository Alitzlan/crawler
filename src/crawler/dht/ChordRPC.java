package crawler.dht;

import java.rmi.Remote;

/*
 * Remote procedure calls for Chord
 * Created by Chi
 */

public interface ChordRPC extends Remote{
    public ChordNode find_successor(int id);
    public ChordNode find_predecessor(int id);
    public ChordNode closest_preceding_finger(int id);

    public void join(ChordNode n);
    public void init_finger_table(ChordNode n);
    public void update_others();
    public void update_finger_table(ChordNode s, int i);
}

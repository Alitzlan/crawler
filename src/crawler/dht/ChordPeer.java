package crawler.dht;

import crawler.common.NodeInfo;
import crawler.common.UrlInfo;

/*
 * The object for remote procedure calls
 * Created by Chi
 */

public class ChordPeer extends NodeInfo {
    public UrlInfo lookUp(int key) {
        return null;
    }

    public ChordPeer find_successor() {
        return null;
    }

    public ChordPeer find_predecessor() {
        return null;
    }

    public ChordPeer closest_preceding_finger(int id) {
        return this;
    }
}

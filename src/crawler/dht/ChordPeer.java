package crawler.dht;

import crawler.common.NodeInfo;
import crawler.common.UrlInfo;

/*
 * The object for remote procedure calls
 * Created by Chi
 */

public class ChordPeer extends NodeInfo implements ChordRPC {

    @Override
    public ChordPeer find_successor(int id) {
        return null;
    }

    @Override
    public ChordPeer find_predecessor(int id) {
        return null;
    }

    @Override
    public ChordPeer closest_preceding_finger(int id) {
        return null;
    }
}

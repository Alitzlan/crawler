package crawler.dht;

import crawler.common.NodeInfo;
import crawler.common.UrlInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/*
 * The object that provides node look up service using finger table
 * Created by Chi
 */

public class ChordNode extends ChordPeer {
    private class Finger extends ChordPeer {
        int start;
        IntRange range;
    }

    private Finger[] finger_table = new Finger[Chord.FINGER_TABLE_SIZE];
    private Hashtable<Integer, UrlInfo> hashtable;

    public ChordNode() {

    }

    @Override
    public ChordPeer find_successor() {
        return null;
    }

    @Override
    public ChordPeer find_predecessor() {
        ChordPeer pred = this;
        IntRange testrange;
        testrange = new IntRange(this.id, id, Chord.MAX_NUM_OF_NODE);
        
        return null;
    }

    @Override
    public ChordPeer closest_preceding_finger(int id) {
        IntRange testrange = new IntRange(this.id, id, Chord.MAX_NUM_OF_NODE);
        for (int i = Chord.FINGER_TABLE_SIZE - 1; i >= 0; i--) {
            if(testrange.containOpenOpen(finger_table[i].id))
                return finger_table[i];
        }
        return this;
    }

    public void printFingerTable() {
        Logger logger = LoggerFactory.getLogger(ChordNode.class);
        logger.info(String.format("start\tinterval\tsuccessor"));
        for (Finger finger : finger_table) {
            logger.info(String.format("%d\t[%d, %d)\t%d", finger.start, finger.range.getMin(), finger.range.getMax(), finger.id));
        }
    }
}

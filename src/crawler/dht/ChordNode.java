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

public class ChordNode extends NodeInfo implements ChordRPC {
    private class Finger extends ChordNode {
        int start;
        IntRange range;
    }

    private Finger[] finger_table = new Finger[Chord.FINGER_TABLE_SIZE];
    private Hashtable<Integer, UrlInfo> hashtable;

    public ChordNode() {

    }

    public ChordNode find_successor(int id) {
        ChordNode pred_for_id = find_predecessor(id);
        return pred_for_id.finger_table[0];
    }

    public ChordNode find_predecessor(int id) {
        ChordNode pred_for_id = this;
        IntRange test_range;
        for (test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].id);
             !test_range.containOpenClose(id);
             test_range = new IntRange(pred_for_id.id, pred_for_id.finger_table[0].id))
            pred_for_id = pred_for_id.closest_preceding_finger(id);
        return pred_for_id;
    }

    public ChordNode closest_preceding_finger(int id) {
        IntRange testrange = new IntRange(this.id, id, Chord.MAX_NUM_OF_NODE);
        for (int i = Chord.FINGER_TABLE_SIZE - 1; i >= 0; i--)
            if (testrange.containOpenOpen(finger_table[i].id))
                return finger_table[i];
        return this;
    }

    public void join(ChordNode n) {

    }

    public void init_finger_table(ChordNode n) {

    }

    public void update_others() {

    }
    
    public void update_finger_table(ChordNode s, int i) {

    }

    public void printFingerTable() {
        Logger logger = LoggerFactory.getLogger(ChordNode.class);
        logger.info(String.format("start\tinterval\tsuccessor"));
        for (Finger finger : finger_table) {
            logger.info(String.format("%d\t[%d, %d)\t%d", finger.start, finger.range.getMin(), finger.range.getMax(), finger.id));
        }
    }
}

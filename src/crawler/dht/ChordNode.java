package crawler.dht;

import crawler.common.NodeInfo;
import crawler.common.UrlInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import static crawler.dht.Chord.FINGER_TABLE_SIZE;

/*
 * The object that provides node look up service using finger table
 * Created by Chi
 */

public class ChordNode extends NodeInfo implements ChordRPC {
    private class Finger {
        int start;
        IntRange range;
        ChordNode node;
    }

    private Finger[] finger_table = new Finger[FINGER_TABLE_SIZE];
    private Hashtable<Integer, UrlInfo> hashtable;
    private ChordNode predecessor;

    public ChordNode() {

    }

    public ChordNode find_successor(int id) {
        ChordNode pred_for_id = find_predecessor(id);
        return pred_for_id.finger_table[0].node;
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
        for (int i = FINGER_TABLE_SIZE - 1; i >= 0; i--)
            if (testrange.containOpenOpen(finger_table[i].id))
                return finger_table[i].node;
        return this;
    }

    public void join(ChordNode n) {
        if(n.id != 0) {
            init_finger_table(n);
            update_others();
            // TODO: move keys responsibility from successor
        }
        else {
            for(int i = 0; i < FINGER_TABLE_SIZE; i++)
                finger_table[i].node = this;
            predecessor = this;
        }
    }

    public void init_finger_table(ChordNode n) {
        finger_table[0].node = n.find_predecessor(finger_table[0].start);
        predecessor = finger_table[0].node.predecessor;
        finger_table[0].node.predecessor = this;
        IntRange testrange;
        for(i = 0; i < FINGER_TABLE_SIZE-1; i++) {
            testrange = new IntRange(this.id, finger_table[i].node.id);
            if(testrange.containCloseOpen(finger_table[i+1].start))
                finger_table[i+1].node = finger_table[i].node;
            else
                finger_table[i+1].node = n.find_successor(finger_table[i+1].start);
        }
    }

    public void update_others() {
        for(int i = 0; i < FINGER_TABLE_SIZE; i++) {
            ChordNode p = find_predecessor(this.id - (int) Math.pow(2, i - 1));
            p.update_finger_table(this, i);
        }
    }

    public void update_finger_table(ChordNode s, int i) {
        IntRange testrange = new IntRange(this.id, finger_table[i].node.id);
        if(testrange.containCloseOpen(s.id)) {
            finger_table[i].node = s;
            p = predecessor;
            predecessor.update_finger_table(s,i);
        }
    }

    public void printFingerTable() {
        Logger logger = LoggerFactory.getLogger(ChordNode.class);
        logger.info(String.format("start\tinterval\tsuccessor"));
        for (Finger finger : finger_table) {
            logger.info(String.format("%d\t[%d, %d)\t%d", finger.start, finger.range.getMin(), finger.range.getMax(), finger.id));
        }
    }
}

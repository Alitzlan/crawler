package crawler.test;

import crawler.dht.ChordMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * The test main function for Chord message
 * Created by Chi
 */

public class TestChordMsg {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(TestLogger.class);
        ChordMsg msg = new ChordMsg();
        String jsonstr = ChordMsg.toJson(msg);
        logger.info(String.format("To JSON: %s", jsonstr));
        ChordMsg newmsg = ChordMsg.fromJson(jsonstr);
        logger.info(String.format("From JSON: type %s, val %s", newmsg.type, newmsg.val));
    }
}

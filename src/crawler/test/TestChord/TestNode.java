package crawler.test.TestChord;

import crawler.dht.ChordNode;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNode {
    public static void main(String[] args) throws ParseException {
        Logger logger = LoggerFactory.getLogger(TestNode.class);
        ChordNode.main(args);
    }
}

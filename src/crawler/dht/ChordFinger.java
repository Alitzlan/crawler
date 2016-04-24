package crawler.dht;

import java.io.Serializable;

/*
 * Finger info for Chord
 * Created by Chi
 */
public class ChordFinger implements Serializable {
    int start;
    IntRange range;
    ChordNodeInfo node;
}

package crawler.dht;

import crawler.common.NodeInfo;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/*
 * Additional node info for Chord
 * Created by Chi
 */

public class ChordNodeInfo extends NodeInfo implements Serializable{

    public ChordFinger[] finger_table;
    public ChordNodeInfo predecessor;

    public ChordNodeInfo() {
    }

    public ChordNodeInfo(String socketaddr) throws URISyntaxException {
        URI uri = new URI("rpc://"+socketaddr);
        hostname = uri.getHost();
        addr = new InetSocketAddress(uri.getHost(), uri.getPort());
    }
}

package crawler.common;

import java.io.Serializable;
import java.net.InetSocketAddress;

/*
 * Common information of a node
 * Created by Chi
 */

public class NodeInfo implements Serializable {
    public short id;
    public String hostname;
    public InetSocketAddress addr; //ipaddr + port
}

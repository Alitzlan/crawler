package crawler.common;

import java.net.InetSocketAddress;

/*
 * Common information of a node
 * Created by Chi
 */

public class NodeInfo {
    public short id;
    public String hostname;
    public InetSocketAddress addr; //ipaddr + port
}

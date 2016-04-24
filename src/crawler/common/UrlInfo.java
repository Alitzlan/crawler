package crawler.common;

import java.io.Serializable;
import java.util.Date;

/*
 * Common information of a URL
 * Created by Chi
 */

public class UrlInfo implements Serializable {
    public String url;
    public Date timestamp;
    public int priority;                    // static category priority for pure FIFO implementation of concurrent queue
    public int dynamicPriority;             // dynamic priority calculated from URL for skiplist concurrent priority quque implementation

    public UrlInfo(String url, int priority, int dynamicPriority) {
        this.url = url;
        this.timestamp = new Date();
        this.priority = priority;
        this.dynamicPriority = dynamicPriority;
    }
}

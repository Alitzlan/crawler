package crawler.common;

import java.util.Date;

/*
 * Common information of a URL
 * Created by Chi
 */

public class UrlInfo {
    public String url;
    public Date timestamp;
    public int priority;

    public UrlInfo(String url) {
        this.url = url;
        this.timestamp = new Date();
        this.priority = 0;
    }
}

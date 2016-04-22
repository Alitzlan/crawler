package crawler.test;

import org.apache.commons.codec.digest.DigestUtils;

import static crawler.dht.ChordPolicy.MAX_NUM_OF_NODE;
import static crawler.dht.ChordPolicy.SHA1_SUBSTR_BEGIN;

/**
 * Created by Alitz-YC on 4/22/2016.
 */
public class TestHash {
    public static void main(String[] args) {
        String sha1 = DigestUtils.sha1Hex("www.google.com.hk");
        System.out.println(sha1);
        System.out.println(sha1.length());
        long testlong = Long.parseUnsignedLong(sha1.substring(SHA1_SUBSTR_BEGIN), 16);
        System.out.println(String.format("%d", testlong%MAX_NUM_OF_NODE));
    }
}

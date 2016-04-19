package crawler.dht;

import com.google.gson.Gson;

/*
 * The object for storing message contents
 * Created by Chi
 */

public class ChordMsg {
    private static Gson gson = new Gson();

    public enum MsgType {
        NOTIFY, LOOKUP, RPC
    }

    public MsgType type;
    public int val;

    public ChordMsg() {
        type = MsgType.NOTIFY;
        val = 0;
    }

    // convert to json string
    public static String toJson(ChordMsg msg) {
        return gson.toJson(msg);
    }

    // load from json string
    public static ChordMsg fromJson(String jsonstr) {
        return gson.fromJson(jsonstr, ChordMsg.class);
    }
}

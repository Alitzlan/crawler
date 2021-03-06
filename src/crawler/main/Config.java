package crawler.main;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by javid on 5/4/16.
 */
public class Config {
    private int id;
    private int queuePort;
    private int dhtPort;
    private int leadershipPort;
    private int heartBeatPort;
    private int txPythonPort;
    private int rxPythonPort;
    private String hostname;
    private String peerList[];

    public Config(int id, String peerList[]) throws UnknownHostException {
        this.id = id;
        this.peerList = peerList;
        hostname = InetAddress.getLocalHost().getHostName();

        // use hardcoded ports
        queuePort = 5000;
        dhtPort = 5001;
        leadershipPort = 5002;
        heartBeatPort = 5003;
        txPythonPort = 5004;
        rxPythonPort = 5005;
    }
    public void setId(int id) { this.id = id; }

    public int getId() {
        return id;
    }

    public int getQueuePort() {
        return queuePort;
    }

    public int getDhtPort() {
        return dhtPort;
    }

    public String getHostname() {return hostname; }

    public int getLeadershipPort() {
        return leadershipPort;
    }

    public int getHeartBeatPort() {
        return heartBeatPort;
    }

    public int getTxPythonPort() {
        return txPythonPort;
    }

    public int getRxPythonPort() {
        return rxPythonPort;
    }

    public String[] getPeerList() {
        return peerList;
    }
}

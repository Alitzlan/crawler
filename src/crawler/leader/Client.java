package crawler.leader;

import crawler.main.Config;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by javid on 5/3/16.
 */
public class Client {
    Logger logger = LoggerFactory.getLogger(Client.class);
    protected static final int time_quatum = 1000;
    enum state {ELECTION_IN_PROGRESS,ELECTION,KNOWN_LEADER, LEADER}

    public static state currState = state.ELECTION;
    public static Lock stateLock = new ReentrantLock();
    private Config config;
    private HeartBeatThread hbThreadObject;
    private Thread hbThread;
    private Thread readerThread;
    private int maxCrashes = 1;

    public Client(Config config){
        this.config = config;
    }

    public void RunLeadershipElection(){
        DatagramSocket sock;
        DatagramSocket hbSock;
        InetAddress ia;
        String peer;
        byte[] sendata;
        String leader;
        Queue<String> list = new ConcurrentLinkedDeque<>();

        try {
            // open up the sockets for communication and configure them
            sock = new DatagramSocket(config.getLeadershipPort());
            hbSock = new DatagramSocket(config.getHeartBeatPort());
            hbSock.setSoTimeout(time_quatum);

            // create and startup reader & hb thread
            hbThreadObject = new HeartBeatThread(config,hbSock);
            hbThread = new Thread(hbThreadObject);
            readerThread = new Thread(new ReaderThread(list,sock,config));

            hbThread.start();
            readerThread.start();

            // signal all other hosts that a new node has come online for an election to occur
            for(int i = 0; i < config.getPeerList().length; i++){
                peer = config.getPeerList()[i];

                if(config.getHostname().compareTo(peer) != 0) {
                    sendata = "-1".getBytes();
                    ia = InetAddress.getByName(config.getPeerList()[i]);
                    DatagramPacket sendPacket = new DatagramPacket(sendata, sendata.length, ia, config.getLeadershipPort());
                    sock.send(sendPacket);
                }
            }

            // align this process up with all other processes to be in sync on time quatums
            int sync = (int)(Client.time_quatum - (System.currentTimeMillis() % Client.time_quatum));
            Thread.sleep(sync);

            // enter normal operation for consensus on leader
            while(true){

                switch(currState){
                    case KNOWN_LEADER: {
                        // clear the list of election candidates
                        list.clear();

                        // CRAWL!!!
                        break;
                    }
                    case ELECTION: {
                        // update that we are in an election process
                        stateLock.lock();
                        currState = state.ELECTION_IN_PROGRESS;
                        stateLock.unlock();

                        logger.info(String.format("Node %d: begin another leader election", config.getId()));
                        for(int i = 0; i <= maxCrashes; i++){
                            BroadCast(sock, config);

                            // delay till the next round of communication
                            sync = (int)(Client.time_quatum - (System.currentTimeMillis() % Client.time_quatum));
                            Thread.sleep(sync);
                        }

                        // we have ran f+1 iterations of broadcast select minimum id for leader
                        list.add(String.format("%d:%s", config.getId(), config.getHostname()));
                        leader = minimum(list);
                        logger.info(String.format("Node %s is elected as a new leader", leader));

                        if(currState == state.ELECTION_IN_PROGRESS) {
                            logger.info("updateing state");
                            stateLock.lock();
                            if (leader.compareTo(config.getHostname()) == 0) {
                                currState = state.LEADER;
                                config.setId(0);
                            }
                            else {
                                currState = state.KNOWN_LEADER;
                                hbThreadObject.setLeader(leader);
                            }
                            stateLock.unlock();
                            list.clear();
                        }
                        break;
                    }
                    case LEADER:{
                        // Run the non-blocking queue and you are the 1st DHT node
                        break;
                    }
                    default:
                        logger.error("I shouldn't be here" + currState.toString());

                }
            }


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void BroadCast(DatagramSocket sock, Config config) throws IOException {
        String peer;
        byte sendata[];
        InetAddress ia;

        for(int i = 0; i < config.getPeerList().length; i++){
            peer = config.getPeerList()[i];

            if(config.getHostname().compareTo(peer) != 0) {
                sendata = String.format("%d:%s", config.getId(), config.getHostname()).getBytes();
                ia = InetAddress.getByName(config.getPeerList()[i]);
                DatagramPacket sendPacket = new DatagramPacket(sendata, sendata.length, ia, config.getLeadershipPort());
                sock.send(sendPacket);
            }
        }
    }

    public String minimum(Queue<String> list){
        int minimumID = Integer.MAX_VALUE, id;
        String ret = "";
        String split[];

        for(String s : list){
            split = s.split(":");
            id = Integer.valueOf(split[0]);

            if(id < minimumID){
                minimumID = id;
                ret = split[1];
            }
        }
        return ret;
    }
}
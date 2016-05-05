package crawler.leader;

import crawler.dht.ChordNode;
import crawler.dht.ChordNodeInfo;
import crawler.dht.ChordRPC;
import crawler.main.Config;
import crawler.nonblockingqueue.QueueClient;
import crawler.nonblockingqueue.QueueServer;
import crawler.pythonRxTx.PythonRxTxThread;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;


/**
 * Created by javid on 5/3/16.
 */
public class Client {
    Logger logger = LoggerFactory.getLogger(Client.class);
    protected static final int time_quatum = 1000;
    public static enum state {ELECTION_IN_PROGRESS,ELECTION,KNOWN_LEADER, LEADER}

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

    public void RunNode(){
        // leadership variables
        DatagramSocket sock;                                    // socket to communicate on for leadership election
        DatagramSocket hbSock;                                  // heartbeat socket
        InetAddress ia;                                         // internet address of peers
        String peer;                                            // string representation of fqdm of peer to communicate with
        byte[] sendata;                                         // data to send
        String leader = "";                                     // fqdm of elected leader
        Queue<String> list = new ConcurrentLinkedDeque<>();     // list of proposed leaders

        // DHT variables
        ChordNode chordNode = null;
		Registry registry;
		ChordRPC stub;

        // concurrent queue client variables
        QueueClient qc = null;
        PythonRxTxThread rxtx = null;

        // concurrent queue server variables
        QueueServer qs = null;
        Thread queueServerThread = null;


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
                        /*********************************************************************************
                         *
                         * If node is in this state then the leader is known and it can start crawling.
                         * Steps that should occur next:
                         *  1) start up DHT node
                         *  2) join DHT 1st node is the leader
                         *  3) run DHT node
                         *  4) query queue at leader for next URL
                         *  5) crawl URL
                         *  6) add url to DHT
                         *  7) goto 3
                         *
                         ********************************************************************************/
                        // this is the first iteration after an initial election I am not the leader so start up
                        // queue client, DHT node, and python interaction threads to begin crawl
                        if(chordNode == null && qc == null){
                            chordNode = new ChordNode((short)config.getId(),config.getDhtPort());
							stub = (ChordRPC) UnicastRemoteObject.exportObject(chordNode, 0);
							
							// handle RMI registry stuff
							registry = LocateRegistry.getRegistry();
							registry.rebind("ChordRPC" + chordNode.myinfo.addr.getPort(), stub);
							
							// join chord network
	                        chordNode.join(new ChordNodeInfo(leader, config.getDhtPort()));

                            // Chi something needs to updated here as Chord keeps failing

                            qc = new QueueClient(leader, config.getQueuePort());
                            rxtx = new PythonRxTxThread(qc, chordNode, config);

                            // start communication with python crawler
                            new Thread(rxtx).start();
                        }
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
                    case LEADER: {
                        /*********************************************************************************
                         *
                         * If node is in this state then it is the first node and is the leader/master
                         * Steps that should occur next:
                         *  1) start up non-blocking quque
                         *  2) start up first DHT node
                         *  3) run non-blocking queue
                         *  4) run DHT node
                         *  7) goto 3
                         *
                         ********************************************************************************/
                        // on the first iteration startup DHT and queue processes
                        if(chordNode == null && qs == null){
							chordNode = new ChordNode((short)config.getId(),config.getDhtPort());
							stub = (ChordRPC) UnicastRemoteObject.exportObject(chordNode, 0);
							
							// handle RMI registry stuff
							registry = LocateRegistry.getRegistry();
							registry.rebind("ChordRPC" + chordNode.myinfo.addr.getPort(), stub);
							
							// join chord network as master node
                            chordNode.join(null);
                            logger.info("I am here chord created");
                            // Chi something needs to updated here as Chord keeps failing

                            (new Thread(){
								public void run() {
									try{
										QueueServer.start(config.getQueuePort());
									} catch (Exception e){
										e.printStackTrace();
										System.exit(1);
									}
								}
							}).start();
                            logger.info("queue server started");
                        }
                        //logger.info("I am leader");
                        break;
                    }
                    default:
                        logger.error("I shouldn't be here" + currState.toString());

                }
                sync = (int)(Client.time_quatum - (System.currentTimeMillis() % Client.time_quatum));
                Thread.sleep(sync);
            }


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
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

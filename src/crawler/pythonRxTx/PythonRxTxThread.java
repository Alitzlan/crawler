package crawler.pythonRxTx;

import crawler.dht.ChordNode;
import crawler.leader.Client;
import crawler.main.Config;
import crawler.nonblockingqueue.QueueClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.rmi.NotBoundException;

/**
 * Created by javid on 5/5/16.
 */
public class PythonRxTxThread implements Runnable {
    private QueueClient qc;
    private Config config;
    private ChordNode chordNode;
    private DatagramSocket sock;
    Logger logger = LoggerFactory.getLogger(Client.class);

    public PythonRxTxThread(QueueClient qc, ChordNode chordNode, Config config) {
        this.qc = qc;
        this.chordNode = chordNode;
        this.config = config;
    }

    @Override
    public void run() {
        String data;
        byte[] receiveData;
        byte[] sendData;
        DatagramPacket receivePacket;
        DatagramPacket sendPacket;
        URL url;
        String nextCrawl;

        try {
            sock = new DatagramSocket(config.getRxPythonPort());

            //upon first starting start at /r/programming and go from there
            if(chordNode.lookup("https://www.reddit.com/r/programming") == null){
                nextCrawl = "https://www.reddit.com/r/programming";
                sendData = nextCrawl.getBytes();
            }
            else{   // we have already crawled that url grab one from the queue to start
                nextCrawl = qc.dequeue().getUrl();
                sendData = nextCrawl.getBytes();
            }

            // add url to DHT
            chordNode.insert(nextCrawl);

            // send initial url to python to crawl
            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), config.getTxPythonPort());
            sock.send(sendPacket);
            logger.info("Sent " + nextCrawl + " to crawl");

            while (true) {
                if (Client.currState == Client.state.KNOWN_LEADER) {
                    receiveData = new byte[1024];
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // get url from python crawler
                    sock.receive(receivePacket);
                    data = new String(receivePacket.getData());

                    try{
                        // try to create url object out of data
                        url = new URL(data);

                        // if it works then we have a url from the crawler
                        if(chordNode.lookup(data) == null){
                            logger.info("Adding URL: " + data + " to frontier queue");
                            qc.enqueue(data);
                        }
                        else{
                            logger.debug("URL: " + data + " found in DHT");
                        }
                    }
                    catch(MalformedURLException e){
                        // this was not a ulr but an ACK from crawler. Feed it the next URL
                        nextCrawl = qc.dequeue().getUrl();
                        sendData = nextCrawl.getBytes();

                        // add url to DHT
                        chordNode.insert(nextCrawl);

                        // send url to python to crawl
                        sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), config.getTxPythonPort());
                        sock.send(sendPacket);
                        logger.info("Sent " + nextCrawl + " to crawl");
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }
}

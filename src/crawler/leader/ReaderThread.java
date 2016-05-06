package crawler.leader;

import crawler.main.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by javid on 5/3/16.
 */
public class ReaderThread implements Runnable{
    private Config config;
    private Queue<String> list;
    private DatagramSocket sock;
    Logger logger = LoggerFactory.getLogger(Client.class);

    public ReaderThread(Queue<String> list, DatagramSocket sock, Config config){
        this.list = list;
        this.sock = sock;
        this.config = config;
    }

    @Override
    public void run() {
        String data;
        byte[] receiveData;
        DatagramPacket receivePacket;


        while(true){
            try {
                receiveData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                sock.receive(receivePacket);
                data = new String(receivePacket.getData());
                logger.info("Received " + data);
                if(!data.contains(":")){
                    // we have been signaled for a new round of election
                    Client.stateLock.lock();
                    Client.currState = Client.state.ELECTION;
                    Client.stateLock.unlock();
                }
                else{
                    list.add(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

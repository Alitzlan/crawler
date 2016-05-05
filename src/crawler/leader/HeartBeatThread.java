package crawler.leader;

import crawler.main.Config;

import java.io.IOException;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by javid on 5/3/16.
 */
public class HeartBeatThread implements Runnable{
    private Config config;
    private DatagramSocket hbSocket;
    private String leader = null;
    Logger logger = LoggerFactory.getLogger(HeartBeatThread.class);

    public HeartBeatThread(Config config, DatagramSocket hbSocket){
        this.hbSocket = hbSocket;
        this.config = config;
    }

    public void setLeader(String leader){
        this.leader = leader;
    }

    @Override
    public void run() {
        byte[] sendData = new byte[]{(byte)0xAA, (byte)0XFF, (byte)0XAA, (byte)0XFF, (byte)0xBB,0x11, (byte)0xBB,0x11};
        byte[] receiveData = new byte[8];
        InetAddress leaderAddress;
        DatagramPacket sendPacket;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            // align this process up with all other processes to be in sync on time quatums
            int sync = (int)(Client.time_quatum - (System.currentTimeMillis() % Client.time_quatum));
            Thread.sleep(sync);

            while(true){
                if(Client.currState == Client.state.LEADER) {
                    while (Client.currState == Client.state.LEADER) {
                        // receive packet from node and echo contents back for heartbeat
                        try {
                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            hbSocket.receive(receivePacket);
                            //logger.debug("Received heartbeat from: " + receivePacket.getAddress());

                            sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), receivePacket.getAddress(), config.getHeartBeatPort());
                            hbSocket.send(sendPacket);
                        }
                        catch(SocketTimeoutException e){
                            // ignore this exception as we are the leader and no one has ping'ed us for a heartbeat
                        }
                    }
                }
                else if(Client.currState == Client.state.KNOWN_LEADER){
                    leaderAddress = InetAddress.getByName(leader);
                    while(Client.currState == Client.state.KNOWN_LEADER){
                        // send packet to leader
                        sendPacket = new DatagramPacket(sendData, sendData.length, leaderAddress, config.getHeartBeatPort());
                        hbSocket.send(sendPacket);

                        // attempt to receive heartbeat
                        try {
                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            hbSocket.receive(receivePacket);
                        }
                        catch (SocketTimeoutException e){
                            logger.error("LEADER HAS CRASHED EXITING.....");
                            System.exit(1);
                        }
                        sync = (int)(Client.time_quatum - (System.currentTimeMillis() % Client.time_quatum));
                        Thread.sleep(sync);
                    }
                }
                Thread.sleep(500);
            }


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

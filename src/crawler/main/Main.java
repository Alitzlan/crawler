package crawler.main;

import crawler.leader.Client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class Main{

    public static void main(String args[]){
        String line;
        String hostname;
        ArrayList<String> peerList = new ArrayList<String>();
        int nodeID = -1;
        Config config;
        Client client;

        // currently we assume that the filepath to the peerlist is passed as arg[0]
        // TODO change to use java options
        String peerListPath = args[0];

        try {
            // get current machines hostname
            hostname = InetAddress.getLocalHost().getHostName();

            // open up peer list file and generate config object
            BufferedReader br = new BufferedReader(new FileReader(peerListPath));
            while((line = br.readLine()) != null){
                String lineData[] = line.split(" ");
                peerList.add(lineData[1]);

                if(hostname.compareTo(lineData[1]) == 0){
                   nodeID = Integer.valueOf(lineData[0]);
                }
            }
            config = new Config(nodeID, peerList.toArray(new String[peerList.size()]));
            client = new Client(config);
            client.RunLeadershipElection();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
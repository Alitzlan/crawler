package crawler.nonblockingqueue;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class QueueServer implements Runnable {
    Socket mySocket;
    //LockFreeQueue<QueueUrl> myQueue = new LockFreeQueue<QueueUrl>();
    StaticConcurrentPriorityQueue myQueue;

    public QueueServer(Socket csocket, StaticConcurrentPriorityQueue queue) {
        this.mySocket = csocket;
        this.myQueue = queue;

    }

    public QueueServer(int port) throws IOException {
        this.mySocket = (new ServerSocket(port)).accept();
        this.myQueue = new StaticConcurrentPriorityQueue();
    }


    public static void main(String args[])
            throws Exception {
        ServerSocket ssock = new ServerSocket(2002);
        //LockFreeQueue<QueueUrl> q = new LockFreeQueue<QueueUrl>();
        StaticConcurrentPriorityQueue queue = new StaticConcurrentPriorityQueue();
        System.out.println("Server is Listening");
        while (true) {
            Socket sock = ssock.accept();
            System.out.println("Connected");
            new Thread(new QueueServer(sock, queue)).start();
        }
    }

    public static void start(int port) throws Exception {
        ServerSocket ssock = new ServerSocket(port);
        //LockFreeQueue<QueueUrl> q = new LockFreeQueue<QueueUrl>();
        StaticConcurrentPriorityQueue queue = new StaticConcurrentPriorityQueue();
        System.out.println("Server is Listening");
        while (true) {
            Socket sock = ssock.accept();
            System.out.println("Connected");
            new Thread(new QueueServer(sock, queue)).start();
        }

    }


    @Override
    public void run() {
        try {
            InputStream is = mySocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            QueueUrl iqu = (QueueUrl) ois.readObject();
            QueueUrl oqu;

            OutputStream os = mySocket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            System.out.println("new url comming: " + iqu.getUrl() + " " + iqu.getAction());
            switch (iqu.getAction()) {
                case "dequeue":

                    oqu = this.myQueue.dequeue();
                    oos.writeObject(oqu);
                    break;
                case "enqueue":
                    if (myQueue.enqueue(iqu))
                        oos.writeObject("OK");
                    else
                        oos.writeObject("unable to enqueue");
                    break;
            }
            is.close();
            ois.close();
            os.close();
            oos.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
    }
}
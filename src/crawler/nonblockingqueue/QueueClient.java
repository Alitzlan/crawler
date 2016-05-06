package crawler.nonblockingqueue;

import java.io.*;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class QueueClient {
    private String host;
    private int port;

    public QueueClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public QueueClient() {
        this.host = "localhost";
        this.port = 2002;
    }

    public QueueUrl dequeue() {
        try {
            Socket s = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            QueueUrl qu = new QueueUrl("", "dequeue");
            oos.writeObject(qu);
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            QueueUrl result = (QueueUrl) ois.readObject();

            oos.close();
            ois.close();
            s.close();
            return result;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public boolean enqueue(String url) {
        try {
            QueueUrl qu = new QueueUrl(url, "enqueue");
            Socket s = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(qu);
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            String result = (String) ois.readObject();
            System.out.println(result);
            return result.equals("OK");
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    public static void main(String[] args) {
        QueueClient q = new QueueClient();
        q.enqueue("https://www.reddit.com/user/ani625");
        QueueUrl output = q.dequeue();
        if (output != null)
            System.out.println(output.getUrl());

		/*Scanner s = new Scanner(System.in);
		while(true){
			System.out.println("please input command: ");
			String input = s.nextLine();
			String[] strSplit = input.split(" ");
			if(strSplit[0].equals("dequeue")){
				System.out.println("suppose to dequeue ");
				QueueUrl output = q.dequeue();
				if(output != null)
					System.out.println("actually deque "+output.getUrl());
				else
					System.out.println("actually deque null");
			}else if(strSplit[0].equals("enqueue")){
				if(q.enqueue(new QueueUrl(strSplit[1],"enqueue"))){
					System.out.println("enqueue succesfully");
				}else{
					System.out.println("enqueue fail");
				}
			}else if(strSplit[1].equals("exit")){
				break;
			}else{
				System.out.println("unknown command!");;
			}
		}*/

    }
}

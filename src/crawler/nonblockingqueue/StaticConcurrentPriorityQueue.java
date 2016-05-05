package crawler.nonblockingqueue;
import java.util.*;
import java.net.*;
public class StaticConcurrentPriorityQueue{
	private ArrayList<LockFreeQueue<QueueUrl>>  queues;
	private int priorityLevel; 
	
	public StaticConcurrentPriorityQueue(int priorityLevel){
		this.queues = new ArrayList<LockFreeQueue<QueueUrl>>();
		this.priorityLevel = priorityLevel;
		for(int i= 0;i<priorityLevel;i++){
			this.queues.add(new LockFreeQueue<QueueUrl>());
		}
	}
	
	//default 5
	public StaticConcurrentPriorityQueue(){
		this.queues = new ArrayList<LockFreeQueue<QueueUrl>>();
		for(int i= 0;i<5;i++){
			this.queues.add(new LockFreeQueue<QueueUrl>());
		}
	}
	
	//use to assign priority to an item, should be re-implement for different kind of item
	public int getPriority(QueueUrl q) {
		String[] contents = q.getUrl().split("/");
		if(contents.length == 5 && contents[3].equals("user")){
			return 0;
		}else if(contents.length>6 && contents[5].equals("comments")){
			return 1;
		}else if(contents.length==6 && contents[5].equals("comments")){
			return 2;
		}else if(contents.length==6){
			return 3;
		}else{
			return 4; 
		}
	}
	
	
	
	public boolean enqueue(QueueUrl q){
		//insert into corresponding queue
		return this.queues.get(getPriority(q)).enqueue(q);
	}
	
	public QueueUrl dequeue(){
		QueueUrl curr = null;
		for(LockFreeQueue<QueueUrl> q:queues){
			curr = q.dequeue();
			if(curr!=null)
				return curr;
		}
		return curr;
		
	}
	
	

}

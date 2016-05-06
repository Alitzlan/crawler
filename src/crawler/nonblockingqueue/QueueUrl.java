package crawler.nonblockingqueue;
import java.io.Serializable;

public class QueueUrl implements Serializable, Comparable<QueueUrl>{
	private String url;
	private String action;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public QueueUrl(String url,String action) {
		this.setAction(action);
		this.url = url;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public int compareTo(QueueUrl o) {
		// TODO Auto-generated method stub
		String otherUrl = o.getUrl();
		
		return 0;
	}
	
}

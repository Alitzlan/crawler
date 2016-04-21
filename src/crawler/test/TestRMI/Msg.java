package crawler.test.TestRMI;

public class Msg implements java.io.Serializable{
    public String content;
    public Msg(String str) {
        this.content = str;
    }
}

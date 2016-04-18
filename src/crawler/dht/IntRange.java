package crawler.dht;

/*
 * The class for range comparison
 * Created by Chi
 */

public class IntRange {
    private int min;
    private int max;
    private int mod;

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
        this.mod = 0;
    }

    public IntRange(int min, int max, int mod) {
        this.min = min;
        if (max > min)
            this.max = max;
        else
            this.max = max + mod;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        if (max < mod)
            return max;
        else
            return max - mod;
    }

    public boolean containCloseClose(int val) {
        return val >= min && val <= max;
    }

    public boolean containCloseOpen(int val) {
        return val >= min && val < max;
    }

    public boolean containOpenClose(int val) {
        return val > min && val <= max;
    }

    public boolean containOpenOpen(int val) {
        return val > min && val < max;
    }
}

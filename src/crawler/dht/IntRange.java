package crawler.dht;

import java.io.Serializable;

/*
 * The class for range comparison
 * Created by Chi
 */

public class IntRange implements Serializable {
    public int min;
    public int max;
    public int mod;
    private boolean cyclic;
    private IntRange first;
    private IntRange second;

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
        cyclic = false;
    }

    public IntRange(int min, int max, int mod) {
        this.mod = mod;
        this.max = max%mod;
        this.min = min%mod;
        if (max > min)
            cyclic = false;
        else {
            cyclic = true;
            first = new IntRange(min, mod);
            second = new IntRange(0, max);
        }
    }

    public boolean containCloseClose(int val) {
        if (cyclic) {
            return first.containCloseOpen(val%mod) || second.containCloseClose(val%mod);
        } else
            return val >= min && val <= max;
    }

    public boolean containCloseOpen(int val) {
        if (cyclic) {
            return first.containCloseOpen(val%mod) || second.containCloseOpen(val%mod);
        } else
            return val >= min && val < max;
    }

    public boolean containOpenClose(int val) {
        if (cyclic) {
            return first.containOpenOpen(val%mod) || second.containCloseClose(val%mod);
        } else
            return val > min && val <= max;
    }

    public boolean containOpenOpen(int val) {
        if (cyclic) {
            return first.containOpenOpen(val%mod) || second.containCloseOpen(val%mod);
        } else
            return val > min && val < max;
    }
}

package org.kohsuke.loopy.rr;

/**
 * @author Kohsuke Kawaguchi
 */
public class TFRecord extends RockRidge {
    public final long[] timestamps;

    public TFRecord(long[] timestamps) {
        this.timestamps = timestamps;
    }

    public long getMtime() {
        return timestamps[1];
    }

    public long getAtime() {
        return timestamps[2];
    }

    public long getCtime() {
        return timestamps[3];
    }
}

package org.agilewiki.utils.virtualcow;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates a unique timestamp, up to 1024 per millisecond.
 * When out of timestamps, the code yields.
 * This code is thread safe.
 */
public class Timestamp {
    public static final String PREFIX = "$t";
    static private AtomicLong aLong = new AtomicLong(System.currentTimeMillis() << 10);

    /**
     * Returns an id for the given timestamp.
     *
     * @param timestamp
     * @return The timestamp in hex, prefaced by $t.
     */
    public static String timestampId(long timestamp) {
        return PREFIX + Long.toHexString(timestamp);
    }

    /**
     * Extracts the time from a timestamp.
     *
     * @param timestamp    The timestamp.
     * @return The time in milliseconds.
     */
    public static long time(long timestamp) {
        return timestamp >> 10;
    }

    /**
     * Converts a timestampId to a timestamp in long form.
     *
     * @param timestampId    A timestamp prefixed by $t.
     * @return The equivalent timestamp as a long.
     */
    public static long timestamp(String timestampId) {
        if (!timestampId.startsWith(PREFIX))
            throw new IllegalArgumentException("does not start with " + PREFIX);
        return Long.parseUnsignedLong(timestampId.substring(2), 16);
    }

    /**
     * Extracts the time from a timestampId.
     * @param timestampId    A timestamp prefixed by $t.
     * @return The time in milliseconds.
     */
    public static long time(String timestampId) {
        return time(timestamp(timestampId));
    }

    public static long generate() {
        while (true) {
            long ts = aLong.get();
            long t = time(ts);
            long ct = System.currentTimeMillis();
            if (ct > t) {
                long nts = ct << 10;
                if (aLong.compareAndSet(ts, nts))
                    return nts;
            } else {
                long oldNdx = ts & 1023;
                if (oldNdx == 1023) {
                    Thread.yield();
                } else {
                    long nts = (ct << 10) | (oldNdx + 1);
                    if (aLong.compareAndSet(ts, nts))
                        return nts;
                }
            }
        }
    }
}

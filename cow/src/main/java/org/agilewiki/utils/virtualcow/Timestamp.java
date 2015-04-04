package org.agilewiki.utils.virtualcow;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates a unique timestamp, up to 1024 per millisecond.
 * When out of timestamps, the code yields.
 * This code is thread safe.
 */
public class Timestamp {
    static AtomicLong aLong = new AtomicLong(System.currentTimeMillis() << 10);

    public static long generate() {
        while (true) {
            long ts = aLong.get();
            long t = ts >> 10;
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

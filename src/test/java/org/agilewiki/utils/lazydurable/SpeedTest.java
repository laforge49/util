package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

public class SpeedTest extends TestCase {
    public void test() throws Exception {
        LazyDurableMapNode m1 = LazyDurableMapNode.MAP_NIL;
        int c = 1000;
        long t0 = System.currentTimeMillis();
        for(int i = 0; i < c; ++i) {
            m1 = m1.add("k" + i, "v" + i, i);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Created "+c+" entries in "+(t1 - t0)+" milliseconds");

        System.out.println("durable length = "+m1.getDurableLength());
    }
}

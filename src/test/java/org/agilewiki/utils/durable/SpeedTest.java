package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class SpeedTest extends TestCase {
    public void test() throws Exception {
        DurableMapNode m1 = DurableMapNode.MAP_NIL;
        int c = 1;
        long t0 = System.currentTimeMillis();
        for(int i = 0; i < c; ++i) {
            m1 = m1.add("k" + i, "v" + i, i);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Created "+c+" entries in "+(t1 - t0)+" milliseconds");

        System.out.println("durable length = "+m1.getDurableLength());
        ByteBuffer byteBuffer = ByteBuffer.allocate(m1.getDurableLength());
        m1.writeDurable(byteBuffer);
        byteBuffer.flip();
        DurableMapNode m2 = (DurableMapNode) FactoryRegistry.readId(byteBuffer).deserialize(byteBuffer);
        //String fk = (String) m2.firstKey(Long.MAX_VALUE);
        //System.out.println(fk);
    }
}

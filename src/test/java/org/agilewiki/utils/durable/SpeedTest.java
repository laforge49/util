package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class SpeedTest extends TestCase {
    public void test() throws Exception {
        DurableMapNode m1 = DurableMapNode.MAP_NIL;
        int c = 1000000;
        long t0 = System.currentTimeMillis();
        for(int i = 0; i < c; ++i) {
            m1 = m1.add("k" + i, "v" + i, i);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Created "+c+" entries in "+(t1 - t0)+" milliseconds");

        System.out.println("durable length = "+m1.getDurableLength());
        ByteBuffer byteBufferx = ByteBuffer.allocate(m1.getDurableLength());
        m1.writeDurable(byteBufferx);

        long t2 = System.currentTimeMillis();
        ByteBuffer byteBuffer = ByteBuffer.allocate(m1.getDurableLength());
        m1.writeDurable(byteBuffer);
        long t3 = System.currentTimeMillis();
        System.out.println("Serialization time = "+(t3 - t2)+" milliseconds");
        byteBuffer.flip();
        long t4 = System.currentTimeMillis();
        DurableMapNode m2 = (DurableMapNode) FactoryRegistry.readId(byteBuffer).deserialize(byteBuffer);
        String fk = (String) m2.firstKey(DurableListNode.MAX_TIME);
        m2.set("k0","upd", 2*c);
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(m2.getDurableLength());
        m2.writeDurable(byteBuffer1);
        long t5 = System.currentTimeMillis();
        System.out.println("Deserialize/reserialize time = "+(t5 - t4)+" milliseconds");
    }
}

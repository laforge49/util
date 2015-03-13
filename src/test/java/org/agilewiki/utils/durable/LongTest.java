package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class LongTest extends TestCase {
    public void test() throws Exception {
        FactoryRegistry factoryRegistry = new FactoryRegistry();
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Long long1 = 42L;
        DurableFactory durableFactory1 = factoryRegistry.getDurableFactory(long1);
        assertTrue(durableFactory1 instanceof LongFactory);
        assertEquals(8, durableFactory1.getDurableLength(long1));
        durableFactory1.writeDurable(long1, byteBuffer);
        assertEquals(10, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = factoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof LongFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(10, byteBuffer.position());
        assertTrue(object2 instanceof Long);
        long long2 = (Long) object2;
        assertEquals(42l, long2);
    }
}

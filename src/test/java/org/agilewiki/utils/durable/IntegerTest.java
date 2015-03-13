package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class IntegerTest extends TestCase {
    public void test() throws Exception {
        FactoryRegistry factoryRegistry = new FactoryRegistry();
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Integer integer1 = 42;
        DurableFactory durableFactory1 = factoryRegistry.getDurableFactory(integer1);
        assertTrue(durableFactory1 instanceof IntegerFactory);
        durableFactory1.writeDurable(integer1, byteBuffer);
        assertEquals(6, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = factoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof IntegerFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(6, byteBuffer.position());
        assertTrue(object2 instanceof Integer);
        int int2 = (Integer) object2;
        assertEquals(42, int2);
    }
}

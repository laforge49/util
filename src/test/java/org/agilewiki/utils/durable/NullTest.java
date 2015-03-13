package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class NullTest extends TestCase {
    public void test() throws Exception {
        FactoryRegistry factoryRegistry = new FactoryRegistry();
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Integer integer1 = null;
        DurableFactory durableFactory1 = factoryRegistry.getDurableFactory(integer1);
        assertTrue(durableFactory1 instanceof NullFactory);
        durableFactory1.writeDurable(integer1, byteBuffer);
        assertEquals(2, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = factoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof NullFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(2, byteBuffer.position());
        assertNull(object2);
    }
}

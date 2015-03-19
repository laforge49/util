package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class NullTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Integer integer1 = null;
        DurableFactory durableFactory1 = FactoryRegistry.getDurableFactory(integer1);
        assertTrue(durableFactory1 instanceof NullFactory);
        assertEquals(2, durableFactory1.getDurableLength(integer1));
        durableFactory1.writeDurable(integer1, byteBuffer);
        assertEquals(2, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof NullFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(2, byteBuffer.position());
        assertNull(object2);
    }
}

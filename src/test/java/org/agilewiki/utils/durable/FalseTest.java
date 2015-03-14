package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class FalseTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Boolean boolean1 = false;
        DurableFactory durableFactory1 = FactoryRegistry.getDurableFactory(boolean1);
        assertTrue(durableFactory1 instanceof FalseFactory);
        assertEquals(2, durableFactory1.getDurableLength(boolean1));
        durableFactory1.writeDurable(boolean1, byteBuffer);
        assertEquals(2, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof FalseFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(2, byteBuffer.position());
        assertTrue(object2.equals(false));
    }
}

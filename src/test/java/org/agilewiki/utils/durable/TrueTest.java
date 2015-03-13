package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class TrueTest extends TestCase {
    public void test() throws Exception {
        FactoryRegistry factoryRegistry = new FactoryRegistry();
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Boolean boolean1 = true;
        DurableFactory durableFactory1 = factoryRegistry.getDurableFactory(boolean1);
        assertTrue(durableFactory1 instanceof TrueFactory);
        assertEquals(0, durableFactory1.getDurableLength(boolean1));
        durableFactory1.writeDurable(boolean1, byteBuffer);
        assertEquals(2, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = factoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof TrueFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(2, byteBuffer.position());
        assertTrue(object2.equals(true));
    }
}

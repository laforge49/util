package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class DoubleTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Double double1 = 42.0D;
        DurableFactory durableFactory1 = FactoryRegistry.getDurableFactory(double1);
        assertTrue(durableFactory1 instanceof DoubleFactory);
        assertEquals(8, durableFactory1.getDurableLength(double1));
        durableFactory1.writeDurable(double1, byteBuffer);
        assertEquals(10, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof DoubleFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(10, byteBuffer.position());
        assertTrue(object2 instanceof Double);
        double double2 = (Double) object2;
        assertEquals(42.0D, double2);
    }
}

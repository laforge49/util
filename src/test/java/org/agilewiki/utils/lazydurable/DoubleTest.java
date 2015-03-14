package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class DoubleTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Double double1 = 42.0D;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(double1);
        assertTrue(lazyDurableFactory1 instanceof DoubleFactory);
        assertEquals(10, lazyDurableFactory1.getDurableLength(double1));
        lazyDurableFactory1.writeDurable(double1, byteBuffer);
        assertEquals(10, byteBuffer.position());
        byteBuffer.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(lazyDurableFactory2 instanceof DoubleFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer);
        assertEquals(10, byteBuffer.position());
        assertTrue(object2 instanceof Double);
        double double2 = (Double) object2;
        assertEquals(42.0D, double2);
    }
}

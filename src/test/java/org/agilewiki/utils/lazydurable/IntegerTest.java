package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class IntegerTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Integer integer1 = 42;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(integer1);
        assertTrue(lazyDurableFactory1 instanceof IntegerFactory);
        assertEquals(6, lazyDurableFactory1.getDurableLength(integer1));
        lazyDurableFactory1.writeDurable(integer1, byteBuffer);
        assertEquals(6, byteBuffer.position());
        byteBuffer.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(lazyDurableFactory2 instanceof IntegerFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer);
        assertEquals(6, byteBuffer.position());
        assertTrue(object2 instanceof Integer);
        int int2 = (Integer) object2;
        assertEquals(42, int2);
    }
}

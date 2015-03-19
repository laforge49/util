package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class LongTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Long long1 = 42L;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(long1);
        assertTrue(lazyDurableFactory1 instanceof LongFactory);
        assertEquals(10, lazyDurableFactory1.getDurableLength(long1));
        lazyDurableFactory1.writeDurable(long1, byteBuffer);
        assertEquals(10, byteBuffer.position());
        byteBuffer.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(lazyDurableFactory2 instanceof LongFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer);
        assertEquals(10, byteBuffer.position());
        assertTrue(object2 instanceof Long);
        long long2 = (Long) object2;
        assertEquals(42l, long2);
    }
}

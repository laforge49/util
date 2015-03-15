package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class DurableMapTest extends TestCase {
    public void test() throws Exception {
        LazyDurableMapNode m1 = LazyDurableMapNode.MAP_NIL;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(m1);
        assertTrue(lazyDurableFactory1 instanceof NilMapNodeFactory);
        assertEquals(2, lazyDurableFactory1.getDurableLength(m1));
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(lazyDurableFactory1.getDurableLength(m1));
        lazyDurableFactory1.writeDurable(m1, byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        byteBuffer1.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer1);
        assertTrue(lazyDurableFactory2 instanceof NilMapNodeFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        assertTrue(object2.equals(LazyDurableMapNode.MAP_NIL));

        LazyDurableMapNode m2 = m1;
        m2 = m2.add("a", "1", 1);
        m2 = m2.add("a", "2", 2);
        m2 = m2.add("a", "3", 3);
        LazyDurableFactory lazyDurableFactory3 = FactoryRegistry.getDurableFactory(m2);
        assertTrue(lazyDurableFactory3 instanceof LazyDurableMapNodeFactory);
        assertEquals(144, lazyDurableFactory3.getDurableLength(m2));
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(lazyDurableFactory3.getDurableLength(m2));
        lazyDurableFactory3.writeDurable(m2, byteBuffer2);
        assertEquals(144, byteBuffer2.position());
        byteBuffer2.flip();
        LazyDurableFactory lazyDurableFactory4 = FactoryRegistry.readId(byteBuffer2);
        assertTrue(lazyDurableFactory4 instanceof LazyDurableMapNodeFactory);
        Object object4 = lazyDurableFactory4.deserialize(byteBuffer2);
        assertEquals(144, byteBuffer2.position());
        assertEquals("123", String.join("", ((LazyDurableMapNode) object4).getList("a").flatList(3)));
    }
}

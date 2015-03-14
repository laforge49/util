package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class DurableListTest extends TestCase {
    public void test() throws Exception {
        LazyDurableListNode l1 = LazyDurableListNode.LIST_NIL;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(l1);
        assertTrue(lazyDurableFactory1 instanceof NilListNodeFactory);
        assertEquals(2, lazyDurableFactory1.getDurableLength(l1));
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(lazyDurableFactory1.getDurableLength(l1));
        lazyDurableFactory1.writeDurable(l1, byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        byteBuffer1.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer1);
        assertTrue(lazyDurableFactory2 instanceof NilListNodeFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        assertTrue(object2.equals(LazyDurableListNode.LIST_NIL));

        LazyDurableListNode l2 = l1;
        l2 = l2.add("1", 1);
        l2 = l2.add("2", 2);
        l2 = l2.add("3", 3);
        LazyDurableFactory lazyDurableFactory3 = FactoryRegistry.getDurableFactory(l2);
        assertTrue(lazyDurableFactory3 instanceof LazyDurableListNodeFactory);
        assertEquals(98, lazyDurableFactory3.getDurableLength(l2));
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(lazyDurableFactory3.getDurableLength(l2));
        lazyDurableFactory3.writeDurable(l2, byteBuffer2);
        assertEquals(98, byteBuffer2.position());
        byteBuffer2.flip();
        LazyDurableFactory lazyDurableFactory4 = FactoryRegistry.readId(byteBuffer2);
        assertTrue(lazyDurableFactory4 instanceof LazyDurableListNodeFactory);
        Object object4 = lazyDurableFactory4.deserialize(byteBuffer2);
        assertEquals(98, byteBuffer2.position());
        assertEquals("123", String.join("", ((LazyDurableListNode) object4).flatList(3)));
    }
}

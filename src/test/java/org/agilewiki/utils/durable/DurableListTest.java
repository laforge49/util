package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class DurableListTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(100);
        DurableListNode l1 = DurableListNode.LIST_NIL;
        DurableFactory durableFactory1 = FactoryRegistry.getDurableFactory(l1);
        assertTrue(durableFactory1 instanceof NilListNodeFactory);
        assertEquals(2, durableFactory1.getDurableLength(l1));
        durableFactory1.writeDurable(l1, byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        byteBuffer1.flip();
        DurableFactory durableFactory2 = FactoryRegistry.readId(byteBuffer1);
        assertTrue(durableFactory2 instanceof NilListNodeFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        assertTrue(object2.equals(DurableListNode.LIST_NIL));

        ByteBuffer byteBuffer2 = ByteBuffer.allocate(100);
        DurableListNode l2 = l1;
        l2 = l2.add("1", 1);
        l2 = l2.add("2", 2);
        l2 = l2.add("3", 3);
        DurableFactory durableFactory3 = FactoryRegistry.getDurableFactory(l2);
        assertTrue(durableFactory3 instanceof DurableListNodeFactory);
        assertEquals(98, durableFactory3.getDurableLength(l2));
        durableFactory3.writeDurable(l2, byteBuffer2);
        assertEquals(98, byteBuffer2.position());
        byteBuffer2.flip();
        DurableFactory durableFactory4 = FactoryRegistry.readId(byteBuffer2);
        assertTrue(durableFactory4 instanceof DurableListNodeFactory);
        Object object4 = durableFactory4.deserialize(byteBuffer2);
        assertEquals(98, byteBuffer2.position());
        assertEquals("123", String.join("", ((DurableListNode) object4).flatList(3)));
    }
}

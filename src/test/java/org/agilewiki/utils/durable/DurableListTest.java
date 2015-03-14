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
    }
}

package org.agilewiki.utils.immutable.collections;

import junit.framework.TestCase;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.Registry;

import java.nio.ByteBuffer;

public class ListDurableTest extends TestCase {
    public void test() throws Exception {
        Registry registry = new Registry();

        ListNode l1 = registry.nilList;
        ImmutableFactory factory1 = registry.getImmutableFactory(l1);
        assertTrue(factory1 instanceof NilListNodeFactory);
        assertEquals(2, factory1.getDurableLength(l1));
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(factory1.getDurableLength(l1));
        factory1.writeDurable(l1, byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        byteBuffer1.flip();
        ImmutableFactory factory2 = registry.readId(byteBuffer1);
        assertTrue(factory2 instanceof NilListNodeFactory);
        Object object2 = factory2.deserialize(byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        assertTrue(object2.equals(registry.nilList));

        ListNode l2 = l1;
        l2 = l2.add("1");
        l2 = l2.add("2");
        l2 = l2.add("3");
        ImmutableFactory factory3 = registry.getImmutableFactory(l2);
        assertTrue(factory3 instanceof ListNodeFactory);
        assertEquals(74, factory3.getDurableLength(l2));
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(factory3.getDurableLength(l2));
        factory3.writeDurable(l2, byteBuffer2);
        assertEquals(74, byteBuffer2.position());
        byteBuffer2.flip();
        ImmutableFactory factory4 = registry.readId(byteBuffer2);
        assertTrue(factory4 instanceof ListNodeFactory);
        Object object4 = factory4.deserialize(byteBuffer2);
        assertEquals(74, byteBuffer2.position());
        assertEquals("123", String.join("", ((ListNode) object4).flatList()));
    }
}
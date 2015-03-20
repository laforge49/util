package org.agilewiki.utils.cow.collections;

import junit.framework.TestCase;
import org.agilewiki.utils.cow.Registry;

import java.util.Iterator;

public class VersionedListRemoveTest extends TestCase {
    public void test() throws Exception {
        Registry registry = new Registry();

        VersionedListNode l1 = registry.listNil;

        l1 = l1.remove(-1, 1);
        l1 = l1.remove(0, 1);
        l1 = l1.remove(1, 1);

        VersionedListNode l2 = registry.listNil.add("a", 2);
        l2 = l2.add("b", 3);
        l2 = l2.add("c", 4);
        l2 = l2.add("d", 5);
        l2 = l2.add("e", 6);
        l2 = l2.add("f", 7);
        l2 = l2.add("g", 8);

        l2 = l2.remove(-3, 9);
        l2 = l2.remove(-2, 10);
        l2 = l2.remove(-1, 11);
        assertEquals(7, l2.totalSize());
        l2 = l2.remove(0, 12);
        l2 = l2.remove(1, 13);
        l2 = l2.remove(2, 14);
        l2 = l2.remove(3, 15);
        l2 = l2.remove(4, 16);
        l2 = l2.remove(5, 17);
        l2 = l2.remove(6, 18);
        l2 = l2.remove(7, 19);
        l2 = l2.remove(8, 20);
        assertEquals("abcdefg", String.join("", l2.flatList(8)));
        assertEquals("bcdefg", String.join("", l2.flatList(12)));
        assertEquals("cdefg", String.join("", l2.flatList(13)));
        assertEquals("defg", String.join("", l2.flatList(14)));
        assertEquals("g", String.join("", l2.flatList(17)));
        assertEquals("", String.join("", l2.flatList(21)));

        assertEquals(6, l2.firstIndex(17));
        assertEquals(-1, l2.lastIndex(22));

        VersionedListNode copy = l2.copyList(16);
        assertEquals("e", String.join("", copy.flatList(6)));
        assertEquals("efg", String.join("", copy.flatList(15)));
        assertEquals(3, l2.size(15));
        assertEquals("g", String.join("", copy.flatList(17)));
        Iterator it = copy.iterator(15);
        assertTrue(it.hasNext());
        assertEquals("e", it.next());
        assertTrue(it.hasNext());
        assertEquals("f", it.next());
        assertTrue(it.hasNext());
        assertEquals("g", it.next());
        assertFalse(it.hasNext());

        copy = copy.clearList(30);
        assertEquals(0, copy.size(30));
    }
}

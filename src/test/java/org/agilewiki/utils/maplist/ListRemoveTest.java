package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class ListRemoveTest extends TestCase {
    public void test() throws Exception {
        ListNode l1 = ListNode.LIST_NIL;

        assertNull(l1.remove(-1, 1));
        assertNull(l1.remove(0, 1));
        assertNull(l1.remove(1, 1));

        ListNode l2 = ListNode.LIST_NIL.add("a", 2);
        l2 = l2.add("b", 3);
        l2 = l2.add("c", 4);
        l2 = l2.add("d", 5);
        l2 = l2.add("e", 6);
        l2 = l2.add("f", 7);
        l2 = l2.add("g", 8);

        assertNull(l2.remove(-3, 9));
        assertNull(l2.remove(-2, 10));
        assertNull(l2.remove(-1, 11));
        assertEquals("a", l2.remove(0, 12));
        assertEquals("b", l2.remove(1, 13));
        assertEquals("c", l2.remove(2, 14));
        assertEquals("d", l2.remove(3, 15));
        assertEquals("e", l2.remove(4, 16));
        assertEquals("f", l2.remove(5, 17));
        assertEquals("g", l2.remove(6, 18));
        assertNull(l2.remove(7, 19));
        assertNull(l2.remove(8, 20));
        assertEquals("abcdefg", String.join("", l2.flat(8)));
        assertEquals("bcdefg", String.join("", l2.flat(12)));
        assertEquals("g", String.join("", l2.flat(17)));
        assertEquals("", String.join("", l2.flat(21)));
    }
}

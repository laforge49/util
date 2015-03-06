package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

import java.util.List;

public class ListInsertTest extends TestCase {
    public void test() throws Exception {
        ListNode l1 = ListNode.LIST_NIL.add("a", 2);
        l1 = l1.add("b", 3);
        l1 = l1.add("c", 4);
        l1 = l1.add("d", 5);
        l1 = l1.add("e", 6);
        l1 = l1.add("f", 7);
        l1 = l1.add("g", 8);
        assertEquals(7,l1.maxSize());
        ListAccessor a1 = l1.accessor();
        assertEquals("a", a1.get(0));
        assertEquals("b", a1.get(1));
        assertEquals("c", a1.get(2));
        assertEquals("d", a1.get(3));
        assertEquals("e", a1.get(4));
        assertEquals("f", a1.get(5));
        assertEquals("g", a1.get(6));
        assertEquals("abcdefg", String.join("", a1.flat()));
        assertEquals("abcd", String.join("", l1.flat(5)));

        ListNode l2 = ListNode.LIST_NIL.add(0, "G", 2);
        l2 = l2.add(0, "F", 3);
        l2 = l2.add(0, "E", 4);
        l2 = l2.add(0, "D", 5);
        l2 = l2.add(0, "C", 6);
        l2 = l2.add(0, "B", 7);
        l2 = l2.add(0, "A", 8);
        assertEquals(7,l2.maxSize());
        ListAccessor a2 = l2.accessor();
        assertEquals("A", a2.get(0));
        assertEquals("B", a2.get(1));
        assertEquals("C", a2.get(2));
        assertEquals("D", a2.get(3));
        assertEquals("E", a2.get(4));
        assertEquals("F", a2.get(5));
        assertEquals("G", a2.get(6));
        assertEquals("ABCDEFG", String.join("", a2.flat()));
        assertEquals("DEFG", String.join("", l2.flat(5)));

        assertFalse(a1.isEmpty());
        assertEquals(3, a1.higher(2));
    }
}

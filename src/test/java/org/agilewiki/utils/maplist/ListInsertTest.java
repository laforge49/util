package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class ListInsertTest extends TestCase {
    public void test() throws Exception {
        ListNode ln1 = ListNode.LIST_NIL.add("a", 2);
        ln1 = ln1.add("b", 3);
        ln1 = ln1.add("c", 4);
        ln1 = ln1.add("d", 5);
        ln1 = ln1.add("e", 6);
        ln1 = ln1.add("f", 7);
        ln1 = ln1.add("g", 8);
        assertEquals(7,ln1.maxSize());
    }
}

package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class EmptyListTest extends TestCase {
    public void test() throws Exception {
        ListAccessor la = ListNode.LIST_NIL.accessor();

        assertEquals(ListNode.MAX_TIME, la.time());

        assertNull(la.get(-1));
        assertNull(la.get(0));
        assertNull(la.get(1));

        assertEquals(-1, la.higher(-1));
        assertEquals(-1, la.higher(0));
        assertEquals(-1, la.higher(1));

        assertEquals(-1, la.lower(-1));
        assertEquals(-1, la.lower(0));
        assertEquals(-1, la.lower(1));

        assertTrue(la.isEmpty());

        assertEquals(0, la.flat().size());

        assertEquals(0, ListNode.LIST_NIL.maxSize());
    }
}

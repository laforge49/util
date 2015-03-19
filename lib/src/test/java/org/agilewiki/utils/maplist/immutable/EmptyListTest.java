package org.agilewiki.utils.maplist.immutable;

import junit.framework.TestCase;
import org.agilewiki.utils.maplist.ListAccessor;

public class EmptyListTest extends TestCase {
    public void test() throws Exception {

        assertEquals(0, ImmutableListNode.LIST_NIL.totalSize());

        ListAccessor la = ImmutableListNode.LIST_NIL.listAccessor();

        assertEquals(ImmutableListNode.MAX_TIME, la.time());

        assertNull(la.get(-1));
        assertNull(la.get(0));
        assertNull(la.get(1));

        assertEquals(-1, la.higherIndex(-1));
        assertEquals(-1, la.higherIndex(0));
        assertEquals(-1, la.higherIndex(1));

        assertEquals(-1, la.ceilingIndex(-1));
        assertEquals(-1, la.ceilingIndex(0));
        assertEquals(-1, la.ceilingIndex(1));

        assertEquals(-1, la.lowerIndex(-1));
        assertEquals(-1, la.lowerIndex(0));
        assertEquals(-1, la.lowerIndex(1));

        assertEquals(-1, la.floorIndex(-1));
        assertEquals(-1, la.floorIndex(0));
        assertEquals(-1, la.floorIndex(1));

        assertEquals(-1, la.firstIndex());

        assertEquals(-1, la.lastIndex());

        assertTrue(la.isEmpty());

        assertEquals(0, la.flatList().size());

        assertEquals(-1, la.getIndex(""));
        assertEquals(-1, la.getIndexRight(""));
        assertEquals(-1, la.findIndex(""));
        assertEquals(-1, la.findIndexRight(""));

        assertFalse(la.iterator().hasNext());

        assertEquals(0, la.size());
    }
}

package org.agilewiki.utils.cow.collections;

import junit.framework.TestCase;
import org.agilewiki.utils.cow.Registry;

public class EmptyVersionedListTest extends TestCase {
    public void test() throws Exception {
        Registry registry = new Registry();

        assertEquals(0, registry.listNil.totalSize());

        ListAccessor la = registry.listNil.listAccessor();

        assertEquals(Registry.MAX_TIME, la.time());

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

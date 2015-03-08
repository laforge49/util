package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class EmptyMapTest extends TestCase {
    public void test() throws Exception {

        assertEquals(0, MapNode.MAP_NIL.maxSize(""));

        ListAccessor la = MapNode.MAP_NIL.listAccessor("");
        assertNull(la.get(0));
    }
}

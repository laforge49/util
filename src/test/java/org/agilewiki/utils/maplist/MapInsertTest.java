package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class MapInsertTest extends TestCase {
    public void test() throws Exception {

        MapNode m1 = MapNode.MAP_NIL.add("1", "a", 2);

        assertEquals(1, m1.maxSize("1"));

        assertEquals(1, m1.flatKeys(3).size());

        ListAccessor a1 = m1.listAccessor("1", 3);
        assertEquals("a", a1.get(0));

        assertEquals("a", m1.copyList("1").get(0,4));
    }
}

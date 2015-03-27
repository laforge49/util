package org.agilewiki.utils.virtualcow.collections;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

public class VersionedListInsertTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            int maxRootBlockSize = 10;
            Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize);
            DbFactoryRegistry registry = db.dbFactoryRegistry;

            VersionedListNode l1 = registry.nilVersionedList.add("a", 2);
            assertEquals(1, l1.totalSize());
            l1 = l1.add("b", 3);
            assertEquals(2, l1.totalSize());
            l1 = l1.add("c", 4);
            assertEquals(3, l1.totalSize());
            l1 = l1.add("d", 5);
            l1 = l1.add("e", 6);
            l1 = l1.add("f", 7);
            l1 = l1.add("g", 8);
            assertEquals(7, l1.totalSize());
            ListAccessor a1 = l1.listAccessor();
            assertEquals("a", a1.get(0));
            assertEquals("b", a1.get(1));
            assertEquals("c", a1.get(2));
            assertEquals("d", a1.get(3));
            assertEquals("e", a1.get(4));
            String f = "f";
            assertEquals(f, a1.get(5));
            assertEquals("g", a1.get(6));
            assertEquals("abcdefg", String.join("", a1.flatList()));
            assertEquals("abcd", String.join("", l1.flatList(5)));

            VersionedListNode l2 = registry.nilVersionedList.add(0, "G", 2);
            l2 = l2.add(0, "F", 3);
            l2 = l2.add(0, "E", 4);
            l2 = l2.add(0, "D", 5);
            l2 = l2.add(0, "C", 6);
            l2 = l2.add(0, "B", 7);
            l2 = l2.add(0, "A", 8);
            assertEquals(7, l2.totalSize());
            ListAccessor a2 = l2.listAccessor();
            assertEquals("A", a2.get(0));
            assertEquals("B", a2.get(1));
            assertEquals("C", a2.get(2));
            assertEquals("D", a2.get(3));
            assertEquals("E", a2.get(4));
            assertEquals("F", a2.get(5));
            assertEquals("G", a2.get(6));
            assertEquals(7, a2.size());
            assertEquals("ABCDEFG", String.join("", a2.flatList()));
            assertEquals("DEFG", String.join("", l2.flatList(5)));

            assertFalse(a1.isEmpty());

            assertEquals(0, a1.higherIndex(-3));
            assertEquals(0, a1.higherIndex(-2));
            assertEquals(0, a1.higherIndex(-1));
            assertEquals(1, a1.higherIndex(0));
            assertEquals(2, a1.higherIndex(1));
            assertEquals(3, a1.higherIndex(2));
            assertEquals(4, a1.higherIndex(3));
            assertEquals(5, a1.higherIndex(4));
            assertEquals(6, a1.higherIndex(5));
            assertEquals(-1, a1.higherIndex(6));
            assertEquals(-1, a1.higherIndex(7));
            assertEquals(-1, a1.higherIndex(8));

            assertEquals(0, a1.ceilingIndex(-3));
            assertEquals(0, a1.ceilingIndex(-2));
            assertEquals(0, a1.ceilingIndex(-1));
            assertEquals(0, a1.ceilingIndex(0));
            assertEquals(1, a1.ceilingIndex(1));
            assertEquals(2, a1.ceilingIndex(2));
            assertEquals(3, a1.ceilingIndex(3));
            assertEquals(4, a1.ceilingIndex(4));
            assertEquals(5, a1.ceilingIndex(5));
            assertEquals(6, a1.ceilingIndex(6));
            assertEquals(-1, a1.ceilingIndex(7));
            assertEquals(-1, a1.ceilingIndex(8));

            assertEquals(-1, a1.lowerIndex(-3));
            assertEquals(-1, a1.lowerIndex(-2));
            assertEquals(-1, a1.lowerIndex(-1));
            assertEquals(-1, a1.lowerIndex(0));
            assertEquals(0, a1.lowerIndex(1));
            assertEquals(1, a1.lowerIndex(2));
            assertEquals(2, a1.lowerIndex(3));
            assertEquals(3, a1.lowerIndex(4));
            assertEquals(4, a1.lowerIndex(5));
            assertEquals(5, a1.lowerIndex(6));
            assertEquals(6, a1.lowerIndex(7));
            assertEquals(6, a1.lowerIndex(8));

            assertEquals(-1, a1.floorIndex(-3));
            assertEquals(-1, a1.floorIndex(-2));
            assertEquals(-1, a1.floorIndex(-1));
            assertEquals(0, a1.floorIndex(0));
            assertEquals(1, a1.floorIndex(1));
            assertEquals(2, a1.floorIndex(2));
            assertEquals(3, a1.floorIndex(3));
            assertEquals(4, a1.floorIndex(4));
            assertEquals(5, a1.floorIndex(5));
            assertEquals(6, a1.floorIndex(6));
            assertEquals(6, a1.floorIndex(7));
            assertEquals(6, a1.floorIndex(8));

            assertEquals(0, a1.firstIndex());
            assertEquals(6, a1.lastIndex());

            assertEquals(5, a1.getIndex(f));
            assertEquals(5, a1.getIndexRight(f));
            assertEquals(5, a1.findIndex("f"));
            assertEquals(5, a1.findIndexRight("f"));
        } finally {
            Plant.close();
        }
    }
}

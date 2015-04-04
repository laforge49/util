package org.agilewiki.utils.immutable.collections;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

public class VersionedListEmptyTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            int maxRootBlockSize = 10;
            Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize);
            DbFactoryRegistry registry = db.dbFactoryRegistry;

            assertEquals(0, registry.versionedNilList.totalSize());

            ListAccessor la = registry.versionedNilList.listAccessor();

            assertEquals(db.getTimestamp(), la.time());

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
        } finally {
            Plant.close();
        }
    }
}

package org.agilewiki.utils.virtualcow.collections;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

public class VersionedMapRemoveTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            int maxRootBlockSize = 10;
            Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize);
            DbFactoryRegistry registry = db.dbFactoryRegistry;

            assertEquals(registry.versionedNilMap, registry.versionedNilMap.remove("", 0, 1));
            registry.versionedNilMap.clearMap(1);
            assertEquals(1, registry.versionedNilMap.set("1", "a", 1).size(1));

            VersionedMapNode m1 = registry.versionedNilMap.add("1", "a", 2);
            m1 = m1.remove("1", 0, 3);

            VersionedMapNode m2 = m1.copyMap();
            m2.clearMap(3);
            assertEquals(0, m2.size(4));

            assertEquals(1, m1.totalSize("1"));

            assertEquals(0, m1.flatKeys(4).size());

            MapAccessor ma = m1.mapAccessor();
            assertNull(ma.firstKey());
            assertNull(ma.lastKey());
            assertEquals(0, ma.flatMap().size());

            assertNull(ma.higherKey(""));
            assertNull(ma.ceilingKey(""));
            assertNull(ma.ceilingKey("1"));
            assertNull(ma.lowerKey("9"));
            assertNull(ma.floorKey("9"));
            assertNull(ma.floorKey("1"));

            assertFalse(ma.iterator().hasNext());
        } finally {
            Plant.close();
        }
    }
}

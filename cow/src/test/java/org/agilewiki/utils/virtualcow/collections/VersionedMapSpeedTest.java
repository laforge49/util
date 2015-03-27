package org.agilewiki.utils.virtualcow.collections;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VersionedMapSpeedTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            int maxRootBlockSize = 1000;
            Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize);
            DbFactoryRegistry registry = db.dbFactoryRegistry;

            VersionedMapNode m1 = registry.nilVersionedMap;
            int c = 10;
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < c; ++i) {
                m1 = m1.add("k" + i, "v" + i, i);
            }
            long t1 = System.currentTimeMillis();
            System.out.println("Created " + c + " entries in " + (t1 - t0) + " milliseconds");

            ByteBuffer byteBufferx = ByteBuffer.allocate(m1.getDurableLength());
            m1.writeDurable(byteBufferx);

            long t2 = System.currentTimeMillis();
            ByteBuffer byteBuffer = ByteBuffer.allocate(m1.getDurableLength());
            m1.writeDurable(byteBuffer);
            long t3 = System.currentTimeMillis();
            System.out.println("Serialization time = " + (t3 - t2) + " milliseconds");
            System.out.println("durable length = " + m1.getDurableLength());
            byteBuffer.flip();

            long t4 = System.currentTimeMillis();
            VersionedMapNode m2 = (VersionedMapNode) registry.readId(byteBuffer).deserialize(byteBuffer);
            String fk = (String) m2.firstKey(FactoryRegistry.MAX_TIME);
            m2 = m2.set("k0", "upd", 2 * c);
            ByteBuffer byteBuffer1 = ByteBuffer.allocate(m2.getDurableLength());
            m2.writeDurable(byteBuffer1);
            long t5 = System.currentTimeMillis();
            System.out.println("Deserialize/reserialize time = " + (t5 - t4) + " milliseconds");
            System.out.println("durable length = " + m2.getDurableLength());
        } finally {
            Plant.close();
        }
    }
}

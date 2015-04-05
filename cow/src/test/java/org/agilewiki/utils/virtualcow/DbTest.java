package org.agilewiki.utils.virtualcow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DbTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("vcow.db");
            Files.deleteIfExists(dbPath);
            int maxRootBlockSize = 1000;
            try (Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize)) {
                Files.deleteIfExists(dbPath);
                db.registerTransaction("dbTran", DbTran.class);
                db.open(true);
                db.update("dbTran").call();
                db.close();
                System.out.println(Files.size(dbPath));

                db.open();
                System.out.println(db.mapNode.firstKey());
                System.out.println(db.mapNode.higherKey("w"));
                System.out.println(db.mapNode.lastKey());
                BlockReference br = (BlockReference) db.mapNode.listAccessor("y").get(0);
                System.out.println(br.getData());
                db.close();
            }
        } finally {
            Plant.close();
        }
    }
}

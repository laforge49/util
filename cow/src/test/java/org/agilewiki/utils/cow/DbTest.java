package org.agilewiki.utils.cow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.immutable.collections.MapNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DbTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            int maxRootBlockSize = 1000;
            try (Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize)) {
                Files.deleteIfExists(dbPath);
                db.open(true);
                Transaction t2 = new Transaction() {
                    @Override
                    public MapNode transform(MapNode mapNode) {
                        System.out.println("block usage: " + db.usage());
                        return mapNode.add("x", "hi!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                };
                db.update(t2).call();
                db.close();
                System.out.println(Files.size(dbPath));
                db.open();
                System.out.println(db.mapNode.firstKey());
            }
        } finally {
            Plant.close();
        }
    }
}

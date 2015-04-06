package org.agilewiki.utils.virtualcow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.NameId;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.MapNode;

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

                System.out.println("db file size: " + Files.size(dbPath));

                System.out.println("\nAll keys:");
                MapNode dbMapNode = db.getDbMapNode();
                for (ListAccessor la: dbMapNode.mapAccessor()) {
                    System.out.println(la.key());
                }

                String timestampId = null;
                System.out.println("\nJournal of x:");
                for (String tid: Journal.journal(db, NameId.generate("x"))) {
                    timestampId = tid;
                    System.out.println(tid);
                }

                System.out.println("\nAll items modified by "+timestampId+":");
                for (String id: Journal.modifies(db, timestampId)) {
                    System.out.println(id);
                }

                db.open();
                BlockReference br = (BlockReference) dbMapNode.listAccessor(NameId.generate("y")).get(0);
                System.out.println("\n"+br.getData());
                db.close();
            }
        } finally {
            Plant.close();
        }
    }
}

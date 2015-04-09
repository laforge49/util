package org.agilewiki.utils.virtualcow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.composites.Journal;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.MapAccessor;

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
                String timestampId = db.update("dbTran").call();
                db.close();

                System.out.println("db file size: " + Files.size(dbPath));

                System.out.println("\nAll keys:");
                MapAccessor dbMapAccessor = db.mapAccessor();
                for (ListAccessor la: dbMapAccessor) {
                    System.out.println(la.key());
                }

                System.out.println("\nJournal of x:");
                for (String tid: Journal.journal(db, NameId.generate("x"))) {
                    System.out.println(tid);
                }

                System.out.println("\nAll items modified by " + timestampId + ":");
                for (String id: Journal.modifies(db, timestampId)) {
                    System.out.println(id);
                }

                Display.all(db, db.getTimestamp());
            }
        } finally {
            Plant.close();
        }
    }
}

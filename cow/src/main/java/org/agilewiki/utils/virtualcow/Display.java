package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.MapAccessor;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;

/**
 * Diagnostic display of database contents.
 */
public class Display {
    public static void all(Db db, long timestamp) {
        System.out.println("\n\n--Database Dump--");
        MapAccessor mapAccessor = db.mapAccessor();
        for (ListAccessor la: mapAccessor) {
            VersionedMapNode vmn = (VersionedMapNode) la.get(0);
            vmn(db, vmn.mapAccessor(timestamp), (String) la.key());
        }
    }

    public static void vmn(Db db, MapAccessor ma, String id) {
        System.out.println("\nvmn id: " + id);
        for (ListAccessor la: ma) {
            vln(db, la);
        }
    }

    public static void vln(Db db, ListAccessor la) {
        System.out.println("    " + la.key() + " = " + la.flatList());
    }
}

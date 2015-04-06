package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.NameId;
import org.agilewiki.utils.immutable.collections.MapNode;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;

public class BigVersionedMapTran implements Transaction {
    /**
     * Transforms a map list.
     *
     * @param db        The database to be updated.
     * @param tMapNode  The durable content of the transaction.
     */
    @Override
    public void transform(Db db, MapNode tMapNode) {
        int k = (Integer) tMapNode.getList("k").get(0);
        int I = (Integer) tMapNode.getList("I").get(0);
        for (int i = 0; i < I; i++) {
            db.add(NameId.generate("1"), ""+(k * 10000000 + i), "");
        }
    }
}

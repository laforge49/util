package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.immutable.collections.MapNode;

public class DbTran implements Transaction {
    /**
     * Transforms a map list.
     *
     * @param db        The database to be updated.
     * @param tMapNode  The durable content of the transaction.
     */
    @Override
    public void transform(Db db, MapNode tMapNode) {
        db.set(NameId.generate("x"), "y", 3);
    }
}

package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.collections.MapNode;

public class BigListTran implements Transaction {
    /**
     * Transforms a map list.
     *
     * @param db        The database to be updated.
     * @param tMapNode  The durable content of the transaction.
     * @return The replacement dbMapNode.
     */
    @Override
    public MapNode transform(Db db, MapNode tMapNode) {
        int k = (Integer) tMapNode.getList("k").get(0);
        int I = (Integer) tMapNode.getList("I").get(0);
        MapNode dbMapNode = db.getDbMapNode();
        for (int i = 0; i < I; i++) {
            dbMapNode = dbMapNode.add("0", k*1000000+i);
        }
        return dbMapNode;
    }
}

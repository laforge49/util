package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.collections.MapNode;

public class DbTran implements Transaction {
    /**
     * Transforms a map list.
     *
     * @param db        The database to be updated.
     * @param tMapNode  The durable content of the transaction.
     * @return The replacement dbMapNode.
     */
    @Override
    public MapNode transform(Db db, MapNode tMapNode) {
        MapNode dbMapNode = db.getDbMapNode();
        dbMapNode = dbMapNode.add("x", "hi!");
        BlockReference blockReference =
                new BlockReference(db.dbFactoryRegistry, "ho!");
        dbMapNode = dbMapNode.add("y", blockReference);
        return dbMapNode;
    }
}

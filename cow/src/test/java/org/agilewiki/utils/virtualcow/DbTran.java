package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.collections.MapNode;

public class DbTran implements Transaction {
    /**
     * Transforms a map list.
     *
     * @param dbMapNode The map list to be transformed.
     * @param timestamp A unique timestamp identifying the transaction,
     *                  usable as the time in the versioned API.
     * @param tMapNode  The durable content of the transaction.
     * @return The replacement dbMapNode.
     */
    @Override
    public MapNode transform(MapNode dbMapNode, long timestamp, MapNode tMapNode) {
        dbMapNode = dbMapNode.add("x", "hi!");
        BlockReference blockReference =
                new BlockReference(dbMapNode.getRegistry(), "ho!");
        dbMapNode = dbMapNode.add("y", blockReference);
        return dbMapNode;
    }
}

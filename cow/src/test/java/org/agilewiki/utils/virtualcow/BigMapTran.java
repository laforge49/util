package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.collections.MapNode;

public class BigMapTran implements Transaction {
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
        int k = (Integer) tMapNode.getList("k").get(0);
        int I = (Integer) tMapNode.getList("I").get(0);
        for (int i = 0; i < I; i++) {
            dbMapNode = dbMapNode.add(k * 10000000 + i, "");
        }
        return dbMapNode;
    }
}

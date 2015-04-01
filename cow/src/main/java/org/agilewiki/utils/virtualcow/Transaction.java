package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.virtualcow.collections.MapNode;

import java.io.IOException;

/**
 * A transaction simply transforms a map list.
 */
public interface Transaction {
    /**
     * Transforms a map list.
     *
     * @param mapNode The map list to be transformed.
     * @return The replacement map list.
     */
    MapNode transform(MapNode mapNode)
            throws IOException;

    /**
     * Transaction timeout in milliseconds.
     * Set to Integer.MAX_VALUE by default.
     *
     * @return The max time in milliseconds the transaction can take.
     */
    default int timeoutMillis() {
        return Integer.MAX_VALUE;
    }
}

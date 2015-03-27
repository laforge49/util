package org.agilewiki.utils.cow;

import org.agilewiki.utils.immutable.collections.MapNode;

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
}

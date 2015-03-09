package org.agilewiki.utils.maplist;

import java.util.NavigableSet;

/**
 * Accesses a versioned map list for a given time.
 */
public interface MapAccessor {

    /**
     * Returns a list accessor for the given time.
     *
     * @param key  The key for the list.
     * @return A list accessor for the given time.
     */
    ListAccessor listAccessor(Comparable key);

    /**
     * Returns a set of all keys with non-empty lists for the given time.
     *
     * @return A set of the keys with content at the time of the query.
     */
    NavigableSet flatKeys();
}

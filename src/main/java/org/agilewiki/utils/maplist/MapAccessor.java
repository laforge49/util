package org.agilewiki.utils.maplist;

import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;

/**
 * Accesses a versioned map list for a given time.
 */
public interface MapAccessor {

    /**
     * Returns the time being accessed.
     *
     * @return The time being accessed.
     */
    long time();

    /**
     * Returns the count of all the keys with a non-empty list.
     *
     * @return The current size of the map.
     */
    int size();

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
    NavigableSet<Comparable> flatKeys();

    /**
     * Returns the smallest key of the non-empty lists for the given time.
     *
     * @return The smallest key, or null.
     */
    Comparable firstKey();

    /**
     * Returns the largest key of the non-empty lists for the given time.
     *
     * @return The largest key, or null.
     */
    Comparable lastKey();

    /**
     * Returns the next greater key.
     *
     * @param key     The given key.
     * @return The next greater key with content at the time of the query.
     */
    Comparable higherKey(Comparable key);

    /**
     * Returns a map of all the keys and values present at the given time.
     *
     * @return A map of lists.
     */
    NavigableMap<Comparable, List> flatMap();
}

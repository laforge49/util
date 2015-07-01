package org.agilewiki.awdb.db.cache;

import java.util.Map;

/**
 * An LRU cache which shares references with other caches created by the same CacheFactory.
 */
public interface Cache<K, V> {
    /**
     * Returns the value associated with the key.
     *
     * @param key Key with which the specified value is to be associated.
     * @return The associated value, or null if there is no associated value.
     */
    public V get(Object key);

    /**
     * Associate a value with a key.
     * If there was a value already associated with a key,
     * the value is replaced.
     *
     * @param key   Key with which the specified value is to be associated.
     * @param value The value to be associated with the key,
     *              or null if the value is to be removed from the cache.
     * @return The value previously associated with the key, or null.
     */
    public V put(K key, V value);

    /**
     * Copies all of the mappings from the specified map to the cache.
     * These mappings will replace any mappings the cache had for
     * any of the keys currently in the specified map.
     *
     * @param m Mappings to be stored in the cache.
     * @throws NullPointerException if the specified map is null.
     */
    public void putAll(Map<? extends K, ? extends V> m);
}

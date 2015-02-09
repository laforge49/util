package org.agilewiki.utils.cache;

import org.agilewiki.utils.weakvalues.ConcurrentWeakValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A factory of caches with shared references.
 * Can be helpful when using multiple threads.
 */
public class CacheFactory<K, V> {
    private final int maxCacheSize;
    private int putCount;
    private final ConcurrentWeakValueMap<K, V> map = new ConcurrentWeakValueMap();

    /**
     * Create a CacheFactory
     *
     * @param maxCacheSize Maximum size of each cache.
     *                     I.E. Older entries are purged.
     */
    public CacheFactory(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Create a cache which is not thread safe,
     * but which shares references with other caches
     * created by the same factory.
     * (But the createCache method itself is thread safe.)
     *
     * @return A cache.
     */
    public Map<K, V> createCache() {
        return new Cache();
    }

    private class Cache extends LinkedHashMap<K, V> {
        private Cache() {
            super(16, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxCacheSize;
        }

        @Override
        public V get(Object key) {
            V rv = super.get(key);
            if (rv == null) {
                rv = map.get(key);
                if (rv != null)
                    super.put((K) key, rv);
            }
            return rv;
        }

        @Override
        public V put(K key, V value) {
            V old = super.put(key, value);
            super.put(key, value);
            putCount += 1;
            if (putCount > maxCacheSize) {
                putCount = 0;
                map.poll();
            }
            return old;
        }
    }
}

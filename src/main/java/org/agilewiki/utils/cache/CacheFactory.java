package org.agilewiki.utils.cache;

import org.agilewiki.utils.weakvalues.ConcurrentWeakValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * A factory of caches with shared references.
 * Can be helpful when using multiple threads.
 * </p>
 */
public class CacheFactory<K extends Comparable, V> {
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
     * (The createCache method itself is thread safe.)
     *
     * @return A cache.
     */
    public Cache<K, V> createCache() {
        return new SharingCache();
    }

    private class SharingCache extends LinkedHashMap<K, V> implements Cache<K, V> {
        private SharingCache() {
            super(16, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxCacheSize;
        }

        @Override
        public V get(Object key) {
            V rv = map.get(key);
            super.get(key);
            super.put((K) key, rv);
            return rv;
        }

        @Override
        public V put(K key, V value) {
            if (value == null) {
                V old = map.remove(key);
                super.remove(key);
                return old;
            }
            V old = map.put(key, value);
            super.get(key);
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

package org.agilewiki.utils.weakvalues;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent hash map with weak reference values.
 */
public class ConcurrentWeakValueMap<K, T> {
    public final ConcurrentHashMap<K, KeyedWeakReference<K, T>> map = new ConcurrentHashMap(8, 0.9f, 1);
    private final ReferenceQueue<? super T> q = new ReferenceQueue();

    public KeyedWeakReference<K, T> createKeyedWeakReference(K key, T value) {
        return new KeyedWeakReference(key, value, q);
    }

    public T get(K key) {
        KeyedWeakReference<K, T> r = map.get(key);
        if (r == null)
            return null;
        return r.get();
    }

    public void put(K key, T value) {
        map.put(key, createKeyedWeakReference(key, value));
    }

    /**
     * Call poll occasionally to drop null references.
     */
    public void poll() {
        while(true) {
            KeyedWeakReference<K, T> r = (KeyedWeakReference) q.poll();
            if (r == null)
                return;
            map.remove(r.key);
        }
    }
}

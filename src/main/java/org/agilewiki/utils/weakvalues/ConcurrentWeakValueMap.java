package org.agilewiki.utils.weakvalues;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A concurrent hash map with weak reference values.
 */
public class ConcurrentWeakValueMap<K, T> {
    public final ConcurrentHashMap<K, KeyedWeakReference<K, T>> map = new ConcurrentHashMap(8, 0.9f, 1);
    private final ReferenceQueue<? super T> q = new ReferenceQueue();
    private final AtomicBoolean pollGate = new AtomicBoolean();

    public KeyedWeakReference<K, T> createKeyedWeakReference(K key, T value) {
        return new KeyedWeakReference(key, value, q);
    }

    public T get(Object key) {
        KeyedWeakReference<K, T> r = map.get(key);
        if (r == null)
            return null;
        return r.get();
    }

    public T put(K key, T value) {
        KeyedWeakReference<K, T> r = map.put(key, createKeyedWeakReference(key, value));
        if (r == null)
            return null;
        return r.get();
    }

    public T remove(K key) {
        KeyedWeakReference<K, T> r = map.remove(key);
        if (r == null)
            return null;
        return r.get();
    }

    /**
     * Call poll occasionally to drop null references.
     */
    public void poll() {
        if (!pollGate.weakCompareAndSet(false, true))
            return;
        while(true) {
            KeyedWeakReference<K, T> r = (KeyedWeakReference) q.poll();
            if (r == null) {
                pollGate.set(false);
                return;
            }
            map.remove(r.key);
        }
    }
}

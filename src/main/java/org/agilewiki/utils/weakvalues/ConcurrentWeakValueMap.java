package org.agilewiki.utils.weakvalues;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A concurrent map with weak reference values.
 */
public class ConcurrentWeakValueMap<K, T> {
    /**
     * The wrapped concurrent skip list map.
     */
    public final ConcurrentSkipListMap<K, KeyedWeakReference<K, T>> map =
            new ConcurrentSkipListMap();

    private final ReferenceQueue<? super T> q = new ReferenceQueue();
    private final AtomicBoolean pollGate = new AtomicBoolean();

    /**
     * Create a keyed weak reference.
     *
     * @param key      The associated key.
     * @param value    The wrapped value.
     * @return Returns a weak reference for the given value with the associated key.
     */
    public KeyedWeakReference<K, T> createKeyedWeakReference(K key, T value) {
        return new KeyedWeakReference(key, value, q);
    }

    /**
     * Get the value from the weak reference with the associated key.
     *
     * @param key    The associated key.
     * @return The wrapped value, or null.
     */
    public T get(Object key) {
        KeyedWeakReference<K, T> r = map.get(key);
        if (r == null)
            return null;
        return r.get();
    }

    /**
     * Add a weak reference to the map.
     *
     * @param key   The associated key.
     * @param value The value to be wrapped in the weak reference.
     * @return The key associated with the weak reference.
     */
    public T put(K key, T value) {
        KeyedWeakReference<K, T> r = map.put(key, createKeyedWeakReference(key, value));
        if (r == null)
            return null;
        return r.get();
    }

    /**
     * Remove the weak reference associated with the given key.
     *
     * @param key   The key associated with the weak reference.
     * @return The wrapped value, or null.
     */
    public T remove(K key) {
        KeyedWeakReference<K, T> r = map.remove(key);
        if (r == null)
            return null;
        return r.get();
    }

    /**
     * Call poll occasionally to drop null references.
     * (Thread safe, like all the other methods on this class.)
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

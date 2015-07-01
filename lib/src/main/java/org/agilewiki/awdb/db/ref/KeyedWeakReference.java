package org.agilewiki.awdb.db.ref;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * A weak reference with a key.
 */
public class KeyedWeakReference<K, T> extends WeakReference<T> {
    /**
     * The associated key;
     */
    public final K key;

    /**
     * Creates a new keyed weak reference with the given key that refers to the given object and is
     * registered with the given queue.
     *
     * @param key      the associated key.
     * @param referent object the new weak reference will refer to
     * @param q        the queue with which the reference is to be registered,
     *                 or <tt>null</tt> if registration is not required
     */
    public KeyedWeakReference(K key, T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        this.key = key;
    }
}

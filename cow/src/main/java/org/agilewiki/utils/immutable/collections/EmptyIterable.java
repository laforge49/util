package org.agilewiki.utils.immutable.collections;

import java.util.Iterator;

/**
 * Provides an iterator over nothing.
 */
public class EmptyIterable<T> implements Iterable<T> {
    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return new EmptyIterator<T>();
    }
}

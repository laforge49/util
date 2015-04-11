package org.agilewiki.utils.immutable.collections;

import java.util.Iterator;

/**
 * An iterator over nothing.
 */
public class EmptyIterator<T> implements Iterator<T> {
    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     */
    @Override
    public T next() {
        return null;
    }
}

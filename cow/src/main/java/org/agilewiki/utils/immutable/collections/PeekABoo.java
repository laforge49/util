package org.agilewiki.utils.immutable.collections;

import java.util.Iterator;

/**
 * An iterator that can be positioned.
 */
public interface PeekABoo<T1> extends Iterator<T1>, Iterable<T1> {
    String getPostion();

    void setPosition(String position);

    /**
     * Returns the next value that will be returned.
     *
     * @return The next value to be returned, or null.
     */
    T1 peek();

    @Override
    default PeekABoo<T1> iterator() {
        return this;
    }
}

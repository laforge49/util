package org.agilewiki.utils.immutable.collections;

import java.util.Iterator;

/**
 * An iterator that can be positioned.
 */
public interface PeekABoo<T1> extends Iterator<T1>, Iterable<T1> {
    String getState();

    void setState(String state);

    /**
     * Except for composites, peek often returns the state--which is
     * a lookahead to the next value.
     * @return
     */
    T1 peek();

    @Override
    default PeekABoo<T1> iterator() {
        return this;
    }
}

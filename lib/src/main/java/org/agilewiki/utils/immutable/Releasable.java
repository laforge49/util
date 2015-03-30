package org.agilewiki.utils.immutable;

import java.io.IOException;

/**
 * Immutables supporting the release and resize methods.
 */
public interface Releasable {
    /**
     * release all resources.
     */
    void releaseAll()
            throws IOException;

    /**
     * release the local resources.
     */
    default void releaseLocal()
            throws IOException {}

    /**
     * Resize immutables which are too large.
     *
     * @param maxSize    Max size allowed for durable length.
     * @param maxBlockSize Maximum block size.
     * @return The revised structure.
     */
    default Object resize(int maxSize, int maxBlockSize) throws IOException {
        return this;
    }

    /**
     * Creates a block reference from an immutable.
     * This method is only called on immutables that have
     * a durable length that is not larger than the max block size.
     *
     * @return A block reference.
     */
    default Object shrink() throws IOException {
        throw new UnsupportedOperationException("Unable to shrink");
    }
}

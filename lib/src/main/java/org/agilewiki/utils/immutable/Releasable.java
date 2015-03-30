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
     * @return The revised structure.
     */
    default Object resize(int maxSize) throws IOException {
        return this;
    }
}

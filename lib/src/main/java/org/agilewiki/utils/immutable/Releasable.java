package org.agilewiki.utils.immutable;

import java.io.IOException;

/**
 * Immutables supporting the release method.
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
}

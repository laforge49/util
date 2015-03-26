package org.agilewiki.utils.immutable;

import java.io.IOException;

/**
 * Immutables supporting the release method.
 */
public interface Releasable {
    /**
     * release all resources.
     */
    void release()
            throws IOException;
}

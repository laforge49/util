package org.agilewiki.utils.immutable;

/**
 * Immutables supporting the release method.
 */
public interface Releasable {
    /**
     * release all resources.
     */
    void release();
}

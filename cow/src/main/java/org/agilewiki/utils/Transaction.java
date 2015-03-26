package org.agilewiki.utils;

/**
 * A transaction simply transforms an immutable.
 */
public interface Transaction {
    /**
     * Transforms an immutable.
     *
     * @param immutable The immutable to be transformed.
     * @return The resulting immutable.
     */
    Object transform(Object immutable);
}

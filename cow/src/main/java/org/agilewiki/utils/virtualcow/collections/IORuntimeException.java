package org.agilewiki.utils.virtualcow.collections;

/**
 * Covers an IO exception when needed.
 */
public class IORuntimeException extends RuntimeException {
    public IORuntimeException(Throwable cause) {
        super(cause);
    }
}

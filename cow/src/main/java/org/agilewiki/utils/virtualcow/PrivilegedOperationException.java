package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.BlockIOException;

/**
 * Thrown when a privileged operation is attenpted outside of a transaction.
 */
public class PrivilegedOperationException extends BlockIOException {
}

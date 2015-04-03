package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.BlockIOException;

/**
 * Thrown when creating a block that is larger than the max block size.
 */
public class BlockSizeTooLarge extends BlockIOException {
}

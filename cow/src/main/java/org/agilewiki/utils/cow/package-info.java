/**
 * A basic COW database with support for multiple blocks.
 * Transaction logging is not implemented, recovery depending on
 * one of the two areas where the root block is written being valid.
 */
package org.agilewiki.utils.cow;
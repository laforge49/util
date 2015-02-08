/**
 * <p>A cache for use with multiple threads with minimal locking.</p>
 * <p>Each thread has its own cache, but the caches share references.</p>
 */
package org.agilewiki.utils.cache;
/**
 * <p>
 * CacheFactory creates caches which are not thread-safe.
 * but the individual caches safely share references with
 * other caches created by the same factory.
 * The net effect then is a high-performance, thread-safe cache which does not use locks.
 * </p>
 */
package org.agilewiki.awdb.db.cache;
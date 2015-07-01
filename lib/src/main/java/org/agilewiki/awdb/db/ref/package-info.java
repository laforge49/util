/**
 * <p>
 * A collection of weak reference classes.
 * </p>
 * <p>
 * Reference queues do not always work the way you expect them to,
 * as gc can be slow to determine when an object is only weekly reachable
 * or that more memory is to be obtained by releasing softly reachable objects.
 * </p><p>
 * The consequence to this is that a collection of soft or weak references can become quite large.
 * A HashMap then is not the best choice for a collection of soft or weak references in and of itself,
 * as a HashMap will grow as needed but, unlike structures like ArrayList, it not shrink when the number
 * of entries is reduced significantly.
 * </p>
 * <p>
 * Now with a collections of soft or weak references, you may want to remove those references when their values
 * become null, which can be quickly done if you have the key for those references. To this end we define a
 * subclass of WeakReference which carries the key used in the reference collection.
 * </p>
 */
package org.agilewiki.awdb.db.ref;
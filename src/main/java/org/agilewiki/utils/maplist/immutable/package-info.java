/**
 * <p>
 *     An immutable data structure is one which can not change,
 *     which is in distinct contrast to the unmodifiable data structures
 *     defined in the Collections class which are restricted views of
 *     ordinary lists, maps and sets and which are easily changed.
 * </p>
 * <p>
 *     On the other hand, an immutable data structure can be updated.
 *     This is done by creating a new data structure which often shares
 *     much of the content of the original. Updated immutable data structures
 *     then are created without having to modify the original.
 * </p>
 * <p>
 *     Immutable data structures then are ideal when working with multiple threads.
 * </p>
 * <p>
 *     An immutable versioning map list works a lot like the mutable form.
 *     Only an add will not invalidate older references to the map list
 *     and the method signature for remove now needs to return the new map list
 *     rather than the removed value. The ListAccessor and MapAccessor classes
 *     will work with both mutable and immutable map lists.
 * </p>
 * <p>
 *     Immutable versioning map lists then should be much easier to use safely than
 *     the mutable form.
 * </p>
 */
package org.agilewiki.utils.maplist.immutable;
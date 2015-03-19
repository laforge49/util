/**
 * <h3>A Simple Versioning Map-List</h3>
 * <p>
 *     An entry in a versioning map list has a key, a value,
 *     a creation timestamp and a deletion timestamp,
 *     where the deletion timestamp has a default value of MAX_VALUE.
 *     There is also an order for entries with the same key.
 *     Each value in the map list has a key and a list index,
 *     where the index is not altered by deletions of other entries.
 *     (The index can only be increased when another entry is inserted
 *     earlier in the list for a given key.)
 * </p>
 * <p>
 *     For an implementation, we will use an embellishment of AA trees.
 *     Each node in the map tree contains a level, a left map node pointer,
 *     a right map node pointer, a key and a list node.
 *     List nodes are similar, containing a level,
 *     a left list node pointer, a right list node pointer, a size,
 *     a creation timestamp, a deletion timestamp and a value.
 *     Size is the size of the list sub-tree and is set to
 *     the size of the left list node + the size of the right list node + 1.
 * </p>
 * <p>
 *     Creation and deletion timestamps apply only to the value of
 *     the list node. Searches for a value are always for a given time
 *     and a value is considered present only if it has a
 *     creation timestamp less than or equal to the query time and
 *     a deletion timestamp greater than the query time.
 * </p>
 */
package org.agilewiki.utils.maplist;
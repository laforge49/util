package org.agilewiki.utils.ids.composites;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.ValueId;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.VersionedListNode;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;
import org.agilewiki.utils.virtualcow.Db;

import java.util.Iterator;

/**
 * An implementation of secondary ids for a Versioned Map List (VML).
 */
public class SecondaryId {
    /**
     * Used as a key prefix in a VML key to identify a key/value for a secondary id.
     */
    public static final String SECONDARY_KEY = "$C";

    /**
     * Identifies an id as a composite for a secondary id.
     */
    public static final String SECONDARY_ID = "$D";

    /**
     * Returns a composite key for the value of a secondary id.
     *
     * @param typeId The type of secondary key.
     * @return The composite key.
     */
    public static String secondaryKey(String typeId) {
        NameId.validateId(typeId);
        return SECONDARY_KEY + typeId;
    }

    /**
     * Returns a composite id for a secondary identifier.
     *
     * @param typeId  The type of secondary key.
     * @param valueId The value of the secondary key.
     * @return The composite id.
     */
    public static String secondaryId(String typeId, String valueId) {
        NameId.validateId(typeId);
        ValueId.validateId(valueId);
        return SECONDARY_ID + typeId + valueId;
    }

    public static void validateSecondaryId(String secondaryId) {
        if (!secondaryId.startsWith(SECONDARY_ID + NameId.PREFIX))
            throw new IllegalArgumentException("not a secondary id: " + secondaryId);
        int i = secondaryId.indexOf('$', 4);
        if (i < 0)
            throw new IllegalArgumentException("not a secondary id: " + secondaryId);
        if (!secondaryId.substring(i).startsWith(ValueId.PREFIX))
            throw new IllegalArgumentException("not a secondary id: " + secondaryId);
    }

    /**
     * Returns the name id of the secondary key type.
     *
     * @param secondaryKey A secondary key.
     * @return The name id.
     */
    public static String secondaryKeyType(String secondaryKey) {
        if (!secondaryKey.startsWith(SECONDARY_KEY))
            throw new IllegalArgumentException("not a secondary key: " + secondaryKey);
        String nameId = secondaryKey.substring(2);
        NameId.validateId(nameId);
        return nameId;
    }

    /**
     * Returns the name id of the secondary id type.
     *
     * @param secondaryId A secondary id.
     * @return The name id.
     */
    public static String secondaryIdType(String secondaryId) {
        if (!secondaryId.startsWith(SECONDARY_ID))
            throw new IllegalArgumentException("not a secondary id: " + secondaryId);
        int i = secondaryId.indexOf('$', 3);
        if (i < 0)
            throw new IllegalArgumentException("not a secondary id: " + secondaryId);
        String nameId = secondaryId.substring(2, i);
        NameId.validateId(nameId);
        return nameId;
    }

    /**
     * Returns the value id of the secondary id type.
     *
     * @param secondaryId A secondary id.
     * @return The value id.
     */
    public static String secondaryIdValue(String secondaryId) {
        if (!secondaryId.startsWith(SECONDARY_ID))
            throw new IllegalArgumentException("not a secondary id: " + secondaryId);
        int i = secondaryId.indexOf('$', 3);
        if (i < 0)
            throw new IllegalArgumentException("not a secondary id: " + secondaryId);
        String valueId = secondaryId.substring(i);
        ValueId.validateId(valueId);
        return valueId;
    }

    /**
     * Iterates over all the secondary keys for a given versioned list map.
     *
     * @param vmn       The versioned list map.
     * @param timestamp The time of the query.
     * @return An iterator of list accessors.
     */
    public static Iterable<ListAccessor> secondaryKeyListAccessors(VersionedMapNode vmn, long timestamp) {
        return vmn.iterable(SECONDARY_KEY, timestamp);
    }

    /**
     * Iterates over the ids of the VMLs referenced by a secondary id.
     *
     * @param db          The database.
     * @param secondaryId The secondary id.
     * @param timestamp   The time of the query.
     * @return The Iterable, or null.
     */
    public static Iterable<String> secondaryIdIterable(Db db, String secondaryId, long timestamp) {
        validateSecondaryId(secondaryId);
        ListAccessor listAccessor = db.mapAccessor().listAccessor(secondaryId);
        if (listAccessor == null)
            return null;
        Iterator<ListAccessor> it = ((VersionedMapNode) listAccessor.get(0)).mapAccessor().iterator();
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public String next() {
                        return (String) it.next().key();
                    }
                };
            }
        };
    }

    /**
     * Returns true iff the vmn has the given secondary id.
     *
     * @param db             The database.
     * @param vmlId          The id of the vml.
     * @param secondaryId    The secondary id.
     * @param timestamp      The time of the query.
     * @return True if the secondary key is present.
     */
    public static boolean hasSecondaryId(Db db, String vmlId, String secondaryId, long timestamp) {
        NameId.validateAnId(vmlId);
        VersionedListNode vln = db.versionedListNode(secondaryId, vmlId);
        if (vln == null)
            return false;
        return !vln.isEmpty(timestamp);
    }

    /**
     * Add a secondary key to a vml if not already present.
     *
     * @param db             The database.
     * @param vmlId          The id of the vml.
     * @param secondaryId    The secondary id.
     */
    public static void createSecondaryId(Db db, String vmlId, String secondaryId) {
        if (hasSecondaryId(db, vmlId, secondaryId, db.getTimestamp()))
            return;
        db.set(secondaryId, vmlId, true);
        db.add(vmlId,
                secondaryKey(secondaryIdType(secondaryId)),
                ValueId.value(secondaryIdValue(secondaryId)));
    }

    /**
     * Remove a secondary key from a vml if present.
     *
     * @param db             The database.
     * @param vmlId          The id of the vml.
     * @param secondaryId    The secondary id.
     */
    public static void removeSecondaryId(Db db, String vmlId, String secondaryId) {
        if (!hasSecondaryId(db, vmlId, secondaryId, db.getTimestamp()))
            return;
        db.clearList(secondaryId, vmlId);
        db.remove(vmlId,
                secondaryKey(secondaryIdType(secondaryId)),
                ValueId.value(secondaryIdValue(secondaryId)));
    }
}

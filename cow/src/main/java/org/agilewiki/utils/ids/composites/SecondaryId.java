package org.agilewiki.utils.ids.composites;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.ValueId;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;

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
     * @param type    The type of secondary key.
     * @return The composite key.
     */
    public static String secondaryKey(String type) {
        return SECONDARY_KEY + NameId.generate(type);
    }

    /**
     * Returns a composite id for a secondary identifier.
     *
     * @param type     The type of secondary key.
     * @param value    The value of the secondary key.
     * @return The composite id.
     */
    public static String secondaryId(String type, String value) {
        return SECONDARY_ID + NameId.generate(type) + ValueId.generate(value);
    }

    /**
     * Returns the name id of the secondary key type.
     *
     * @param secondaryKey    A secondary key.
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
     * @param secondaryId    A secondary id.
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
     * Iterates over all the secondary keys for a given versioned list map.
     *
     * @param vmn    The versioned list map.
     * @return An iterator of list accessors.
     */
    public static Iterable<ListAccessor> secondaryKeyListAccessors(VersionedMapNode vmn) {
        return secondaryKeyListAccessors(vmn, vmn.getTimestamp());
    }

    /**
     * Iterates over all the secondary keys for a given versioned list map.
     *
     * @param vmn          The versioned list map.
     * @param timestamp    The time of the query.
     * @return An iterator of list accessors.
     */
    public static Iterable<ListAccessor> secondaryKeyListAccessors(VersionedMapNode vmn, long timestamp) {
        return vmn.iterable(SECONDARY_KEY, timestamp);
    }
}

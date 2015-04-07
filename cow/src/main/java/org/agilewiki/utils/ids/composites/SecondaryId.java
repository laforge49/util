package org.agilewiki.utils.ids.composites;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.ValueId;

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
}

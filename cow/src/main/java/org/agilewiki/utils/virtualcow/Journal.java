package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.NameId;

/**
 * Connecting journal entries to the things they update.
 */
public class Journal {
    /**
     * Prefix to a timestamp/id composite.
     */
    public static final String MODIFIES = "$A";

    /**
     * Prefix to an id/timestamp composite key.
     */
    public static final String JOURNAL_ENTRY = "$B";

    public static String modifiesKey(String timestamp, String id) {
        NameId.validateId(timestamp);
        NameId.validateId(id);
        if (!timestamp.startsWith(Timestamp.PREFIX))
            throw new IllegalArgumentException("not a timestamp id: " + timestamp);
        return MODIFIES + timestamp + id;
    }

    public static String journalEntryKey(String id, String timestamp) {
        NameId.validateId(timestamp);
        NameId.validateId(id);
        if (!timestamp.startsWith(Timestamp.PREFIX))
            throw new IllegalArgumentException("not a timestamp id: " + timestamp);
        return JOURNAL_ENTRY + id + timestamp;
    }
}

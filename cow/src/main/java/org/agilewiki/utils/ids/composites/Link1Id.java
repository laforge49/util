package org.agilewiki.utils.ids.composites;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.ValueId;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.MapAccessor;
import org.agilewiki.utils.immutable.collections.VersionedListNode;
import org.agilewiki.utils.virtualcow.Db;

import java.util.Iterator;

/**
 * An implementation of one-way link ids for a Versioned Map Node (VMN).
 */
public class Link1Id {

    /**
     * Identifies an id as a composite for a link id.
     */
    public static final String LINK1_ID = "$E";

    /**
     * Returns a composite id for a link identifier.
     *
     * @param originId  The originating VMN Id of link.
     * @param labelId The label of the link.
     * @return The composite id.
     */
    public static String link1Id(String originId, String labelId) {
        NameId.validateAnId(originId);
        ValueId.validateAnId(labelId);
        return LINK1_ID + originId + labelId;
    }

    /**
     * Validate a secondary id.
     *
     * @param linkId    The link id.
     */
    public static void validateLink1Id(String linkId) {
        if (!linkId.startsWith(LINK1_ID + "$"))
            throw new IllegalArgumentException("not a link id: " + linkId);
        int i = linkId.indexOf('$', 4);
        if (i < 0)
            throw new IllegalArgumentException("not a link id: " + linkId);
        if (!linkId.substring(i).startsWith(ValueId.PREFIX))
            throw new IllegalArgumentException("not a link id: " + linkId);
    }

    /**
     * Returns the origin id of the link id type.
     *
     * @param linkId A link id.
     * @return The origin id.
     */
    public static String link1IdOrigin(String linkId) {
        if (!linkId.startsWith(LINK1_ID))
            throw new IllegalArgumentException("not a link id: " + linkId);
        int i = linkId.indexOf('$', 3);
        if (i < 0)
            throw new IllegalArgumentException("not a link id: " + linkId);
        String originId = linkId.substring(2, i);
        NameId.validateAnId(originId);
        return originId;
    }

    /**
     * Returns the label id of the link id.
     *
     * @param linkId A link id.
     * @return The label id.
     */
    public static String link1IdLabel(String linkId) {
        if (!linkId.startsWith(LINK1_ID))
            throw new IllegalArgumentException("not a link id: " + linkId);
        int i = linkId.indexOf('$', 3);
        if (i < 0)
            throw new IllegalArgumentException("not a link id: " + linkId);
        String labelId = linkId.substring(i);
        ValueId.validateAnId(labelId);
        return labelId;
    }

    /**
     * Iterates over the label ids of links originating with a VMN.
     *
     * @param db       The database.
     * @param vmnId    The id of the origin VMN.
     * @return An iterable over the label ids of all links.
     */
    public static Iterable<String> link1LabelIdIterable(Db db, String vmnId) {
        MapAccessor ma = db.mapAccessor();
        Iterator<ListAccessor> lait = ma.iterator(LINK1_ID + vmnId);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return lait.hasNext();
                    }

                    @Override
                    public String next() {
                        String linkId = lait.next().key().toString();
                        return link1IdLabel(linkId);
                    }
                };
            }
        };
    }

    /**
     * Iterates over the target VMN ids linked to from a given VMN..
     *
     * @param db     The database.
     * @param vmnId  The id of the originating VMN.
     * @param labelId The label of the links.
     * @param timestamp   The time of the query.
     * @return The Iterable.
     */
    public static Iterable<String> link1IdIterable(Db db, String vmnId, String labelId, long timestamp) {
        return db.keysIterable(link1Id(vmnId, labelId), timestamp);
    }

    /**
     * Returns true iff the link is present.
     *
     * @param db           The database.
     * @param vmnId1       The originating vmn.
     * @param labelId      The link label.
     * @param vmnId2       The target vmn.
     * @param timestamp    The time of the query.
     * @return True if the link exists.
     */
    public static boolean hasLink1(Db db, String vmnId1, String labelId, String vmnId2, long timestamp) {
        String linkId = link1Id(vmnId1, labelId);
        VersionedListNode vln = db.versionedListNode(linkId, vmnId2);
        if (vln == null)
            return false;
        return !vln.isEmpty(timestamp);
    }

    /**
     * Creates a link.
     *
     * @param db           The database.
     * @param vmnId1       The originating vmn.
     * @param labelId      The link label.
     * @param vmnId2       The target vmn.
     */
    public static void createLink1(Db db, String vmnId1, String labelId, String vmnId2) {
        if (hasLink1(db, vmnId1, labelId, vmnId2, db.getTimestamp()))
            return;
        String linkId = link1Id(vmnId1, labelId);
        db.set(linkId, vmnId2, true);
    }

    /**
     * Deletes a link.
     *
     * @param db           The database.
     * @param vmnId1       The originating vmn.
     * @param labelId      The link label.
     * @param vmnId2       The target vmn.
     */
    public static void removeLink1(Db db, String vmnId1, String labelId, String vmnId2) {
        if (!hasLink1(db, vmnId1, labelId, vmnId2, db.getTimestamp()))
            return;
        String linkId = link1Id(vmnId1, labelId);
        db.clearList(linkId, vmnId2);
    }
}

package org.agilewiki.utils.ids.composites;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.MapAccessor;
import org.agilewiki.utils.immutable.collections.VersionedListNode;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;
import org.agilewiki.utils.virtualcow.Db;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of one-way link ids for a Versioned Map Node (VMN).
 */
public class Link1Id {

    /**
     * Identifies an id as a composite for a link id.
     */
    public static final String LINK1_ID = "$E";

    /**
     * Identifies an id as a composite for an inverted link id.
     */
    public static final String LINK1_INV = "$F";

    /**
     * Identifies an id as a composite for a label index id.
     */
    public static final String LABEL1_INDEX_ID = "$G";

    /**
     * Identifies an id as a composite for an inverted label index id.
     */
    public static final String LABEL1_INDEX_INV = "$H";

    /**
     * Returns a composite id for a link identifier.
     *
     * @param originId  The originating VMN Id of link.
     * @param labelId The label of the link.
     * @return The composite id.
     */
    public static String link1Id(String originId, String labelId) {
        NameId.validateAnId(originId);
        NameId.validateAnId(labelId);
        return LINK1_ID + originId + labelId;
    }

    /**
     * Returns a composite id for an inverted link identifier.
     *
     * @param targetId  The target VMN Id of link.
     * @param labelId The label of the link.
     * @return The composite id.
     */
    public static String link1Inv(String targetId, String labelId) {
        NameId.validateAnId(targetId);
        NameId.validateAnId(labelId);
        return LINK1_INV + targetId + labelId;
    }

    /**
     * Returns a composite id for a label identifier.
     *
     * @param originId  The originating VMN Id of link.
     * @param labelId The label of the link.
     * @return The composite id.
     */
    public static String label1IndexId(String originId, String labelId) {
        NameId.validateAnId(originId);
        NameId.validateAnId(labelId);
        return LABEL1_INDEX_ID + labelId + originId;
    }

    /**
     * Returns a composite id for an inverted label identifier.
     *
     * @param targetId  The target VMN Id of link.
     * @param labelId The label of the link.
     * @return The composite id.
     */
    public static String label1IndexInv(String targetId, String labelId) {
        NameId.validateAnId(targetId);
        NameId.validateAnId(labelId);
        return LABEL1_INDEX_INV + labelId + targetId;
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
        NameId.validateAnId(labelId);
        return labelId;
    }

    /**
     * Returns the label id of the link inv.
     *
     * @param linkInv A link inv.
     * @return The label id.
     */
    public static String link1InvLabel(String linkInv) {
        if (!linkInv.startsWith(LINK1_INV))
            throw new IllegalArgumentException("not an inverted link id: " + linkInv);
        int i = linkInv.indexOf('$', 3);
        if (i < 0)
            throw new IllegalArgumentException("not an inverted link id: " + linkInv);
        String labelId = linkInv.substring(i);
        NameId.validateAnId(labelId);
        return labelId;
    }

    /**
     * Returns the vmn id which is the origin of a link with the given label index.
     *
     * @param labelIndexId A label index id.
     * @return The VMN id.
     */
    public static String label1IndexIdOrigin(String labelIndexId) {
        if (!labelIndexId.startsWith(LABEL1_INDEX_ID))
            throw new IllegalArgumentException("not a label index id: " + labelIndexId);
        int i = labelIndexId.indexOf('$', 3);
        if (i < 0)
            throw new IllegalArgumentException("not a label index id: " + labelIndexId);
        String vmnId = labelIndexId.substring(i);
        NameId.validateAnId(vmnId);
        return vmnId;
    }

    /**
     * Returns the vmn id which is the target of a link with the given inverted label index.
     *
     * @param labelIndexInv An inverted label index id.
     * @return The VMN id.
     */
    public static String label1IndexInvTarget(String labelIndexInv) {
        if (!labelIndexInv.startsWith(LABEL1_INDEX_INV))
            throw new IllegalArgumentException("not an inverted label index id: " + labelIndexInv);
        int i = labelIndexInv.indexOf('$', 3);
        if (i < 0)
            throw new IllegalArgumentException("not an inverted label index id: " + labelIndexInv);
        String vmnId = labelIndexInv.substring(i);
        NameId.validateAnId(vmnId);
        return vmnId;
    }

    /**
     * Iterates over the VMNs that are the origin of a link with the given label.
     *
     * @param db           The database.
     * @param labelId      The label id.
     * @param timestamp    The time of the query.
     * @return The iterable.
     */
    public static Iterable<String> label1IdIterable(Db db, String labelId, long timestamp) {
        MapAccessor ma = db.mapAccessor();
        Iterator<ListAccessor> lait = ma.iterator(LABEL1_INDEX_ID + labelId);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    ListAccessor next = null;

                    boolean isNext() {
                        while (next == null && lait.hasNext()) {
                            next = lait.next();
                            VersionedMapNode vmn = (VersionedMapNode) next.get(0);
                            if (vmn == null || vmn.isEmpty(timestamp)) {
                                next = null;
                            }
                        }
                        return next != null;
                    }

                    @Override
                    public boolean hasNext() {
                        return isNext();
                    }

                    @Override
                    public String next() {
                        if (!isNext())
                            throw new NoSuchElementException();
                        String n = label1IndexIdOrigin(next.key().toString());
                        next = null;
                        return n;
                    }
                };
            }
        };
    }

    /**
     * Iterates over the VMNs that are the target of a link with the given label.
     *
     * @param db           The database.
     * @param labelId      The label id.
     * @param timestamp    The time of the query.
     * @return The iterable.
     */
    public static Iterable<String> label1InvIterable(Db db, String labelId, long timestamp) {
        MapAccessor ma = db.mapAccessor();
        Iterator<ListAccessor> lait = ma.iterator(LABEL1_INDEX_INV + labelId);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    ListAccessor next = null;

                    boolean isNext() {
                        while (next == null && lait.hasNext()) {
                            next = lait.next();
                            VersionedMapNode vmn = (VersionedMapNode) next.get(0);
                            if (vmn == null || vmn.isEmpty(timestamp)) {
                                next = null;
                            }
                        }
                        return next != null;
                    }

                    @Override
                    public boolean hasNext() {
                        return isNext();
                    }

                    @Override
                    public String next() {
                        if (!isNext())
                            throw new NoSuchElementException();
                        String n = label1IndexInvTarget(next.key().toString());
                        next = null;
                        return n;
                    }
                };
            }
        };
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
     * Iterates over the label ids of links targeting a VMN.
     *
     * @param db       The database.
     * @param vmnId    The id of the target VMN.
     * @return An iterable over the label ids of all inverted links.
     */
    public static Iterable<String> link1LabelInvIterable(Db db, String vmnId) {
        MapAccessor ma = db.mapAccessor();
        Iterator<ListAccessor> lait = ma.iterator(LINK1_INV + vmnId);
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
                        return link1InvLabel(linkId);
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
     * Iterates over the originating VMN ids targeting a given VMN..
     *
     * @param db     The database.
     * @param vmnId  The id of the target VMN.
     * @param labelId The label of the links.
     * @param timestamp   The time of the query.
     * @return The Iterable.
     */
    public static Iterable<String> link1InvIterable(Db db, String vmnId, String labelId, long timestamp) {
        return db.keysIterable(link1Inv(vmnId, labelId), timestamp);
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
        linkId = link1Inv(vmnId2, labelId);
        db.set(linkId, vmnId1, true);
        String labelIndexId = label1IndexId(vmnId1, labelId);
        db.set(labelIndexId, vmnId2, true);
        labelIndexId = label1IndexInv(vmnId2, labelId);
        db.set(labelIndexId, vmnId1, true);
        if (vmnId1 != db.getJEName())
            db.updateJournal(vmnId1);
        if (vmnId2 != db.getJEName())
            db.updateJournal(vmnId2);
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
        linkId = link1Inv(vmnId2, labelId);
        db.clearList(linkId, vmnId1);
        String labelIndexId = label1IndexId(vmnId1, labelId);
        db.clearList(labelIndexId, vmnId2);
        labelIndexId = label1IndexInv(vmnId2, labelId);
        db.clearList(labelIndexId, vmnId1);
        if (vmnId1 != db.getJEName())
            db.updateJournal(vmnId1);
        if (vmnId2 != db.getJEName())
            db.updateJournal(vmnId2);
    }
}

package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.ValueId;
import org.agilewiki.utils.ids.composites.SecondaryId;
import org.agilewiki.utils.immutable.collections.MapAccessor;
import org.agilewiki.utils.immutable.collections.MapNode;
import org.agilewiki.utils.immutable.collections.VersionedListNode;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;

import java.util.NavigableSet;

public class SecondaryTran implements Transaction {
    @Override
    public void transform(Db db, MapNode tMapNode) {
        String johnJonesId = NameId.generate("JohnJones");
        String JohnJonesVId = ValueId.generate("John Jones");
        String nameTId = NameId.generate("name");
        String johnJonesSID = SecondaryId.secondaryId(nameTId, JohnJonesVId);
        SecondaryId.createSecondaryId(db, johnJonesId, johnJonesSID);
        Display.all(db, db.getTimestamp());
        String id = "$nJohnJones";
        VersionedMapNode vmn = (VersionedMapNode)db.mapAccessor().listAccessor(id).get(0);
        long ts = db.getTimestamp();
        int s = vmn.size(ts);
        System.out.println("size of vmn = " + s);
        NavigableSet<Comparable> ns = vmn.flatKeys(ts);
        System.out.println(ns);
        SecondaryId.removeSecondaryId(db, johnJonesId, johnJonesSID);
        Display.all(db, db.getTimestamp());
        vmn = (VersionedMapNode)db.mapAccessor().listAccessor(id).get(0);
        s = vmn.size(ts);
        System.out.println("size of vmn = "+s);
        ns = vmn.flatKeys(ts);
        System.out.println(ns);
    }
}

package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.ValueId;
import org.agilewiki.utils.ids.composites.SecondaryId;
import org.agilewiki.utils.immutable.collections.MapAccessor;
import org.agilewiki.utils.immutable.collections.MapNode;
import org.agilewiki.utils.immutable.collections.VersionedListNode;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;

public class SecondaryTran implements Transaction {
    @Override
    public void transform(Db db, MapNode tMapNode) {
        String johnJonesId = NameId.generate("JohnJones");
        String JohnJonesVId = ValueId.generate("John Jones");
        String nameTId = NameId.generate("name");
        String johnJonesSID = SecondaryId.secondaryId(nameTId, JohnJonesVId);
        SecondaryId.createSecondaryId(db, johnJonesId, johnJonesSID);
        Display.all(db, db.getTimestamp());
        SecondaryId.removeSecondaryId(db, johnJonesId, johnJonesSID);
        Display.all(db, db.getTimestamp());
    }
}

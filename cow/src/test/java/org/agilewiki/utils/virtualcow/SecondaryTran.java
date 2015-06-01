package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.ValueId;
import org.agilewiki.utils.ids.composites.SecondaryId;
import org.agilewiki.utils.immutable.collections.MapNode;

public class SecondaryTran implements Transaction {
    @Override
    public void transform(Db db, MapNode tMapNode) {
        long timestamp = db.getTimestamp();
        String johnJonesId = NameId.generate("JohnJones");
        String JohnJonesVId = ValueId.generate("John Jones");
        String nameTId = NameId.generate("name");
        String johnJonesSID = SecondaryId.secondaryId(nameTId, JohnJonesVId);
        SecondaryId.createSecondaryId(db, johnJonesId, johnJonesSID);
        Display.all(db, timestamp);

        System.out.println("\nSecondary ids of "+johnJonesId);
        for (String typeId: SecondaryId.typeIdIterable(db, johnJonesId)) {
            for (String secondaryId: SecondaryId.secondaryIdIterable(db, johnJonesId, typeId, db.getTimestamp())) {
                System.out.println("    "+secondaryId);
            }
        };

        System.out.println("\nVMN IDs for secondary id " + johnJonesSID);
        for (String vmlId: SecondaryId.vmnIdIterable(db, johnJonesSID, timestamp)) {
            System.out.println("    "+vmlId);
        }

        SecondaryId.removeSecondaryId(db, johnJonesId, johnJonesSID);
        Display.all(db, timestamp);

        System.out.println("\nSecondary ids of "+johnJonesId);
        for (String typeId: SecondaryId.typeIdIterable(db, johnJonesId)) {
            for (String secondaryId: SecondaryId.secondaryIdIterable(db, johnJonesId, typeId, db.getTimestamp())) {
                System.out.println("    "+secondaryId);
            }
        };

        System.out.println("\nVMN IDs for secondary id " + johnJonesSID);
        for (String vmlId: SecondaryId.vmnIdIterable(db, johnJonesSID, timestamp)) {
            System.out.println("    "+vmlId);
        }
    }
}

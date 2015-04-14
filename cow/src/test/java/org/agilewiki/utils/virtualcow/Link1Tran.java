package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.composites.Link1Id;
import org.agilewiki.utils.immutable.collections.MapNode;

public class Link1Tran implements Transaction {
    @Override
    public void transform(Db db, MapNode tMapNode) {
        long timestamp = db.getTimestamp();
        String johnJonesId = NameId.generate("JohnJones");
        String jackJonesId = NameId.generate("JackJones");
        String sonId = NameId.generate("son");

        Link1Id.createLink1(db, johnJonesId, sonId, jackJonesId);
        for (String labelId: Link1Id.link1LabelIdIterable(db, johnJonesId)) {
            System.out.println("\nlink1 label: "+labelId);
            for (String targetId: Link1Id.link1IdIterable(db, johnJonesId, labelId, timestamp)) {
                System.out.println("target: "+targetId);
            }
        }

        Display.all(db, timestamp);

        Link1Id.removeLink1(db, johnJonesId, sonId, jackJonesId);
        for (String labelId: Link1Id.link1LabelIdIterable(db, johnJonesId)) {
            System.out.println("\nlink1 label: "+labelId);
            for (String targetId: Link1Id.link1IdIterable(db, johnJonesId, labelId, timestamp)) {
                System.out.println("target: "+targetId);
            }
        }

        Display.all(db, timestamp);
    }
}

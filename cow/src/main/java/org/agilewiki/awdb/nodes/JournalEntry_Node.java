package org.agilewiki.awdb.nodes;

import org.agilewiki.awdb.NodeBase;
import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.collections.MapNode;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.Transaction;

public class JournalEntry_Node extends NodeBase implements Transaction {

    public JournalEntry_Node() {
        super(null, 0L);
    }

    public JournalEntry_Node(String nodeId, long timestamp) {
        super(nodeId, timestamp);
    }

    @Override
    public final void transform(Db db, MapNode tMapNode) {
        transformInit(db, tMapNode);
        process(db, tMapNode);
        getAwDb().addNode(this);
    }

    public void transformInit(Db db, MapNode tMapNode) {
        initialize(db.getJEName(), FactoryRegistry.MAX_TIMESTAMP);
        String transactionName = tMapNode.get(Db.transactionNameId).toString();
        getAwDb().createSecondaryId(db.getJEName(), Key_NodeFactory.NODETYPE_ID,
                NameId.generate(transactionName + ".node"));
    }

    public void process(Db db, MapNode tMapNode) {
        throw new UnsupportedOperationException();
    }
}

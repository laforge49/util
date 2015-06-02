package org.agilewiki.utils.immutable.collections;

/**
 * Iterates over the vmn keys.
 */
public class ListAccessorKeysMap extends PeekABooMap<ListAccessor, String> {

    public ListAccessorKeysMap(PeekABoo<ListAccessor> peekABoo) {
        super(peekABoo);
    }

    @Override
    protected String transform(ListAccessor value) {
        return (String) value.key();
    }
}

package org.agilewiki.utils.immutable.collections;

/**
 * An empty iterator.
 */
public class EmptyPeekABoo implements PeekABoo {
    @Override
    public String getPostion() {
        return null;
    }

    @Override
    public void setPosition(String state) {
    }

    @Override
    public String peek() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String next() {
        return null;
    }
}

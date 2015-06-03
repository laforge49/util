package org.agilewiki.utils.immutable.collections;

/**
 * Transforms values.
 */
abstract public class PeekABooMap<T1, T2> implements PeekABoo<T2> {
    private final PeekABoo<T1> peekABoo;

    public PeekABooMap(PeekABoo<T1> peekABoo) {
        this.peekABoo = peekABoo;
    }

    abstract protected T2 transform(T1 value);

    protected String transformString(String value) {
        return value;
    }

    protected String reverseTransformString(String value) {
        return value;
    }

    @Override
    public String getPosition() {
        return transformString(peekABoo.getPosition());
    }

    @Override
    public void setPosition(String position) {
        peekABoo.setPosition(reverseTransformString(position));
    }

    @Override
    public T2 peek() {
        return transform(peekABoo.peek());
    }

    @Override
    public boolean hasNext() {
        return peekABoo.hasNext();
    }

    @Override
    public T2 next() {
        return transform(peekABoo.next());
    }
}

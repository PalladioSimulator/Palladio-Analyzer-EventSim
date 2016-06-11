package edu.kit.ipd.sdq.eventsim.rvisualization.model;

/**
 * A generic pair (2-tuple) implementation, with both elements of the same type.
 * 
 * @author Philipp Merkle
 *
 * @param T
 *            the type of the elements
 */
public class Pair<T> {

    private T first;

    private T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

}

package edu.kit.ipd.sdq.eventsim.api;

/**
 * A procedure that takes no arguments and returns no results, intended to be used with lambda
 * expressions.
 * 
 * @author Philipp Merkle
 *
 */
@FunctionalInterface
public interface Procedure {

    public void execute();

}

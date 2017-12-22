package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

/**
 * Functional interface that maps a simulation element (actual implementation of
 * the element) to an element of an instrumentation description
 * 
 * @author Henning Schulz
 * 
 * @param <S>
 *            simulation element type (actual implementation)
 * @param <M>
 *            model element type (part of the instrumentation description)
 */
public interface SimulationElementMapping<S, M> {

	M get(S simulationElement);

}

package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

/**
 * Represents a pair of a simulation element and a model element. Is used for
 * specification of an element if implementations of a model element cannot be
 * retrieved by an {@link Instrumentor} itself.
 * 
 * @author Henning Schulz
 *
 * @param <S>
 *            simulation element type (actual implementation)
 * @param <M>
 *            model element type (part of the instrumentation description)
 */
public class SEMPair<S, M> {

	private final S simulationElement;
	private final M modelElement;

	public SEMPair(S simulationElement, M modelElement) {
		super();
		this.simulationElement = simulationElement;
		this.modelElement = modelElement;
	}

	public S getSimulationElement() {
		return simulationElement;
	}

	public M getModelElement() {
		return modelElement;
	}

}

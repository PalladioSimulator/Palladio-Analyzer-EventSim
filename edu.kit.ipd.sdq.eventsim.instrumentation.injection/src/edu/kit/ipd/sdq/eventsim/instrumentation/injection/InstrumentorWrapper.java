package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;

/**
 * Wraps an instrumentor which is dealing with elements of the instrumentation
 * description and represents an instrumentor dealing with elements of the
 * simulation engine.
 * 
 * @author Henning Schulz
 *
 * @param <S>
 *            type of the simulation elements (actual implementation; part of
 *            the simulation engine)
 * @param <M>
 *            type of the model elements (part of the instrumentation
 *            description)
 * @param <C>
 *            type of the {@link ProbeConfiguration}
 */
public class InstrumentorWrapper<S, M, C extends ProbeConfiguration> implements Instrumentor<S, C> {

	private final SimulationElementMapping<? super S, M> mapping;
	private final Instrumentor<? super M, C> modelInstrumentor;

	public InstrumentorWrapper(SimulationElementMapping<? super S, M> mapping,
			Instrumentor<? super M, C> modelInstrumentor) {
		this.mapping = mapping;
		this.modelInstrumentor = modelInstrumentor;
	}

	@Override
	public void instrument(S element) {
		modelInstrumentor.instrument(mapping.get(element));
	}

	@Override
	public void instrumentAll() {
		modelInstrumentor.instrumentAll();
	}

}

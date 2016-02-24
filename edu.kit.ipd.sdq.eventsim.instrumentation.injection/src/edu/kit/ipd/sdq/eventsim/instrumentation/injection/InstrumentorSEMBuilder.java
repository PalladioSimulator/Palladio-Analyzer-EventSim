package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import org.osgi.framework.Bundle;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;

/**
 * Lets the user decide if the created instrumentor should deal with model
 * elements or with the actual implementation of the model elements. In the
 * former case, {@link InstrumentorSEMBuilder#withoutMapping() withoutMapping()}
 * should be called. In the latter case, a mapping of simulation to model
 * elements should be specified by callin
 * {@link InstrumentorSEMBuilder#withMapping(SimulationElementMapping)
 * withMapping(SimulationElementMapping)}.
 * 
 * @author Henning Schulz
 *
 * @param <M>
 *            type of the model element the created instrumentor should deal
 *            with
 * 
 * @see SimulationElementMapping
 */
public class InstrumentorSEMBuilder<M> {

	private final PCMModel pcm;
	private final MeasurementStorage storage;
	private final Bundle bundle;
	private final InstrumentationDescription description;

	private final Class<M> modelType;

	public InstrumentorSEMBuilder(PCMModel pcm, MeasurementStorage storage, Bundle bundle,
			InstrumentationDescription description, Class<M> modelType) {
		this.pcm = pcm;
		this.storage = storage;
		this.bundle = bundle;
		this.description = description;
		this.modelType = modelType;
	}

	public <S> InstrumentorInstantiator<S, M> withMapping(SimulationElementMapping<S, M> mapping) {
		return new InstrumentorInstantiator<>(pcm, storage, bundle, description, modelType, mapping);
	}

	public MappinglessInstrumentorInstantiator<M> withoutMapping() {
		return new MappinglessInstrumentorInstantiator<>(pcm, storage, bundle, description, modelType);
	}

}

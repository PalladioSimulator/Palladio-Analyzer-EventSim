package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import org.osgi.framework.Bundle;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;

/**
 * Creates {@link Instrumentor}s dealing directly with elements of the
 * instrumentation description.
 * 
 * @author Henning Schulz
 *
 * @param <M>
 *            type of the model elements (part of the instrumentation
 *            description)
 */
public class MappinglessInstrumentorInstantiator<M> {

	private final PCMModel pcm;
	private final MeasurementStorage storage;
	private final Bundle bundle;
	private final InstrumentationDescription description;
	private final Class<M> modelType;

	public MappinglessInstrumentorInstantiator(PCMModel pcm, MeasurementStorage storage, Bundle bundle,
			InstrumentationDescription description, Class<M> modelType) {
		this.pcm = pcm;
		this.storage = storage;
		this.bundle = bundle;
		this.description = description;
		this.modelType = modelType;
	}

	public <C extends ProbeConfiguration> Instrumentor<M, C> createFor(C configuration) {
		if (ActionRepresentative.class.isAssignableFrom(modelType)) {
			Instrumentor<ActionRepresentative, C> modelInstrumentor = new ActionInstrumentor<C>(
					storage, bundle, description, pcm, configuration);
			return new InstrumentorWrapper<>((M a) -> (ActionRepresentative) a, modelInstrumentor);
		} else if (UserActionRepresentative.class.isAssignableFrom(modelType)) {
			Instrumentor<UserActionRepresentative, C> modelInstrumentor = new UserActionInstrumentor<C>(
					storage, bundle, description, pcm, configuration);
			return new InstrumentorWrapper<>((M a) -> (UserActionRepresentative) a, modelInstrumentor);
		} else {
			return voidInstrumentor();
		}
	}

	private <E, C extends ProbeConfiguration> Instrumentor<E, C> voidInstrumentor() {
		return new Instrumentor<E, C>() {

			@Override
			public void instrument(E element) {
			}

			@Override
			public void instrumentAll() {
			}
		};
	}

}

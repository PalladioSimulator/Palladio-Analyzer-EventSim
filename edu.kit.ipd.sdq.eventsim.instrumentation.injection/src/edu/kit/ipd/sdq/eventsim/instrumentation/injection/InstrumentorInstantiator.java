package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import org.osgi.framework.Bundle;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;

/**
 * Creates {@link Instrumentor}s dealing with elements of the simulation engine.
 * The instrumentor has to map such an element to an element of the
 * instrumentation description by the given {@link SimulationElementMapping}.
 * 
 * @author Henning Schulz
 *
 * @param <S>
 *            type of the simulation elements (actual implementation; part of
 *            the simulation engine)
 * @param <M>
 *            type of the model elements (part of the instrumentation
 *            description)
 */
public class InstrumentorInstantiator<S, M> {

	private final PCMModel pcm;
	private final MeasurementStorage storage;
	private final Bundle bundle;
	private final InstrumentationDescription description;
	private final Class<M> modelType;
	private final SimulationElementMapping<S, M> mapping;

	public InstrumentorInstantiator(PCMModel pcm, MeasurementStorage storage, Bundle bundle,
			InstrumentationDescription description, Class<M> modelType, SimulationElementMapping<S, M> mapping) {
		this.pcm = pcm;
		this.storage = storage;
		this.bundle = bundle;
		this.description = description;
		this.modelType = modelType;
		this.mapping = mapping;
	}

	@SuppressWarnings("unchecked")
	public <C extends ProbeConfiguration> Instrumentor<S, C> createFor(C configuration) {
		// Create an Instrumentor for the instrumentation description type that
		// should be implemented and wraps it to obtain an Instrumentor for the
		// EventSim entity type
		if (ResourceRepresentative.class.isAssignableFrom(modelType)) {
			// Need to use a pair of the resource representative and the actual
			// resource entity in order to pass the latter to the instrumentor
			Instrumentor<SEMPair<S, ResourceRepresentative>, C> instrumentor = new ResourceInstrumentor<>(storage,
					bundle, description, pcm, configuration);
			return new InstrumentorWrapper<>(r -> new SEMPair<>(r, (ResourceRepresentative) mapping.get(r)),
					instrumentor);
		} else if (ActionRepresentative.class.isAssignableFrom(modelType)) {
			Instrumentor<ActionRepresentative<? extends AbstractAction>, C> modelInstrumentor = new ActionInstrumentor<C>(
					storage, bundle, description, pcm, configuration);
			return new InstrumentorWrapper<>((S a) -> (ActionRepresentative<? extends AbstractAction>) mapping.get(a),
					modelInstrumentor);
		} else if (UserActionRepresentative.class.isAssignableFrom(modelType)) {
			Instrumentor<UserActionRepresentative<? extends AbstractUserAction>, C> modelInstrumentor = new UserActionInstrumentor<C>(
					storage, bundle, description, pcm, configuration);
			return new InstrumentorWrapper<>(
					(S a) -> (UserActionRepresentative<? extends AbstractUserAction>) mapping.get(a),
					modelInstrumentor);
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

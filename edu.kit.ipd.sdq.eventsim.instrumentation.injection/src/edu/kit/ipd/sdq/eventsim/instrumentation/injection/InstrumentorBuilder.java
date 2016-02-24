package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import org.osgi.framework.Bundle;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;

/**
 * A builder for {@link Instrumentor}s. It can be used to create instrumentors
 * without knowing the actual implementation of the instrumentor.
 * 
 * @author Henning Schulz
 *
 */
public class InstrumentorBuilder {

	private PCMModel pcm;
	private MeasurementStorage storage;
	private Bundle bundle;
	private InstrumentationDescription description;

	private InstrumentorBuilder(PCMModel pcm) {
		this.pcm = pcm;
	}

	public static InstrumentorBuilder buildFor(PCMModel pcm) {
		return new InstrumentorBuilder(pcm);
	}

	public InstrumentorBuilder withDescription(InstrumentationDescription description) {
		this.description = description;
		return this;
	}

	public InstrumentorBuilder inBundle(Bundle bundle) {
		this.bundle = bundle;
		return this;
	}

	public InstrumentorBuilder withStorage(MeasurementStorage storage) {
		this.storage = storage;
		return this;
	}

	public <M> InstrumentorSEMBuilder<M> forModelType(Class<M> modelType) {
		return new InstrumentorSEMBuilder<>(pcm, storage, bundle, description, modelType);
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.BinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.CalculatorFactory;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;

/**
 * An instrumentor for resources. Since resources are not known by this
 * instrumentor, {@link Instrumentor#instrumentAll() instrumentAll()} is not
 * implemented. For the same reason, this instrumentor deals with pairs of a
 * {@link ResourceRepresentative} and a resource implementation for
 * specification of a resource.
 * 
 * @author Henning Schulz
 *
 * @param <R>
 *            the type of the resource implementation
 * @param <C>
 *            the type of the {@link ProbeConfiguration}
 * 
 * @see SEMPair
 */
public class ResourceInstrumentor<R, C extends ProbeConfiguration>
		implements Instrumentor<SEMPair<R, ResourceRepresentative>, C> {

	private final MeasurementFacade<C> measurementFacade;
	private final MeasurementStorage measurementStorage;
	private final CalculatorFactory calculatorFactory;
	private final InstrumentationDescription description;

    public ResourceInstrumentor(MeasurementStorage measurementStorage, Bundle bundle,
            InstrumentationDescription description, PCMModel pcm, MeasurementFacade<C> measurementFacade) {
		this.measurementStorage = measurementStorage;
		this.calculatorFactory = new CalculatorFactory(bundle);
		this.measurementFacade = measurementFacade;
		this.description = description;
	}

	@Override
	public void instrument(SEMPair<R, ResourceRepresentative> resourcePair) {
		for (ResourceRule<?> entity : description.getAffectingRules(resourcePair.getModelElement())) {
			instrumentResource(resourcePair.getSimulationElement(), entity);
		}
	}

	/**
	 * Does nothing, since resources are not known
	 */
	@Override
	public void instrumentAll() {
	}

	private <E, P extends ResourceRepresentative> void instrumentResource(E resource, ResourceRule<P> entity) {
		Map<ProbeRepresentative, IProbe<E>> createdProbes = new HashMap<>();

		for (ProbeRepresentative modelProbe : entity.getProbes()) {
			createdProbes.put(modelProbe, measurementFacade.createProbe(resource, modelProbe.getMeasuredProperty()));
		}

		for (CalculatorRepresentative modelCalculator : entity.getCalculators()) {
			measurementFacade.createCalculator(instantiateCalculator(resource, modelCalculator))
					.from(resource, modelCalculator.getFromProbe().getMeasuredProperty())
					.to(resource, modelCalculator.getToProbe().getMeasuredProperty())
					.forEachMeasurement(m -> measurementStorage.put(m));

			createdProbes.remove(modelCalculator.getFromProbe());
			createdProbes.remove(modelCalculator.getToProbe());
		}

		for (Entry<ProbeRepresentative, IProbe<E>> probeEntry : createdProbes.entrySet()) {
			probeEntry.getValue().forEachMeasurement(m -> measurementStorage.put(m));
		}
	}

	private <E> BinaryCalculator<E, E> instantiateCalculator(E resource,
			CalculatorRepresentative modelCalculator) {
		Class<?> resourceType = resource.getClass();
		return (BinaryCalculator<E, E>) calculatorFactory.create(modelCalculator.getMetric(), resourceType, resourceType);
	}

}

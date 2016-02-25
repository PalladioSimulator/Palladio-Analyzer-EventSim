package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.HashSet;
import java.util.Set;

import edu.kit.ipd.sdq.eventsim.measurement.calculator.BinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.CalculatorBuilder;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.IntermediateCalculatorFrom;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;
import edu.kit.ipd.sdq.eventsim.measurement.probe.ProbeFactory;
import edu.kit.ipd.sdq.eventsim.measurement.probe.ProbeLocator;

public class MeasurementFacade<C extends ProbeConfiguration> {

	private ProbeFactory<C> probeFactory;

	private Set<IProbe<?>> existingProbesSet;

	public MeasurementFacade(C configuration, ProbeLocator<C> probeLocator) {
		this.probeFactory = new ProbeFactory<>(configuration, probeLocator);
		this.existingProbesSet = new HashSet<>();
	}

	public <F, S> IntermediateCalculatorFrom<F, S> createCalculator(BinaryCalculator<F, S> calculator) {
		return CalculatorBuilder.create(calculator, this);
	}

	public <E, T> IProbe<E> createProbe(E element, String property, Object... contexts) {
		IProbe<E> probe = probeFactory.create(element, property, contexts);
		if (existingProbesSet.contains(probe)) {
			// TODO perhaps use a map because iterating over the set becomes expensive for many probes
			for (IProbe<?> p : existingProbesSet) {
				if (p.equals(probe)) {
					return (IProbe<E>) p;
				}
			}
			// this code should no be reachable without a programming mistake introduced
			throw new RuntimeException("Could not find probe.");
		}
		// not yet contained in set -> add probe
		existingProbesSet.add(probe);
		return probe;
	}
}

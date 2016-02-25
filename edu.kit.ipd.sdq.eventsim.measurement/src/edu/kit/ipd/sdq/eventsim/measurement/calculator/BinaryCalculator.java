package edu.kit.ipd.sdq.eventsim.measurement.calculator;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementProducer;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;


public interface BinaryCalculator<F, S> extends MeasurementProducer<Pair<F, S>> {

	void setup(IProbe<F> fromProbe, IProbe<S> toProbe);

	Measurement<Pair<F, S>> calculate(Measurement<F> first, Measurement<S> second);
	
}

package edu.kit.ipd.sdq.eventsim.system.calculators;

import org.palladiosimulator.pcm.seff.ExternalCallAction;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPointPair;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Calculator;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.ProbePair;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.AbstractBinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;

@Calculator(metric = "responsetime_of_external_calls", type = Pair.class, fromType = ExternalCallAction.class, toType = ExternalCallAction.class, intendedProbes = {
		@ProbePair(from = "before", to = "after") })
public class ResponseTimeOfExternalCallsCalculator
		extends AbstractBinaryCalculator<ExternalCallAction, ExternalCallAction> {

	@Override
	public void setup(IProbe<ExternalCallAction> fromProbe, IProbe<ExternalCallAction> toProbe) {
		fromProbe.enableCaching();
		toProbe.forEachMeasurement(measurement -> {
			notify(calculate(fromProbe.getLastMeasurementOf(measurement.getWho()), measurement));
		});
	}

	@Override
	public Measurement<Pair<ExternalCallAction, ExternalCallAction>> calculate(
			Measurement<ExternalCallAction> from, Measurement<ExternalCallAction> to) {
		if (from == null) {
			return null;
		}

		double when = to.getWhen();
		double responseTime = to.getValue() - from.getValue();

		MeasuringPoint<Pair<ExternalCallAction, ExternalCallAction>> mp = new MeasuringPointPair<>(from.getWhere(), to.getWhere(), "responsetime", to.getWhere().getContexts());
		return new Measurement<>("RESPONSE_TIME_OF_EXTERNAL_CALLS", mp, to.getWho(), responseTime, when);
	}

}

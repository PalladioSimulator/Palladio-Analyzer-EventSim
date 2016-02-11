package edu.kit.ipd.sdq.eventsim.system.calculators;

import org.palladiosimulator.pcm.seff.ExternalCallAction;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPointPair;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Calculator;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.ProbePair;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.AbstractBinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

@Calculator(metric = "responsetime_of_external_calls", type = Pair.class, fromType = ExternalCallAction.class, toType = ExternalCallAction.class, intendedProbes = {
		@ProbePair(from = "before", to = "after") })
public class ResponseTimeOfExternalCallsCalculator extends
		AbstractBinaryCalculator<Pair<ExternalCallAction, ExternalCallAction>, ExternalCallAction, ExternalCallAction, Request> {

	@Override
	public void setup(IProbe<ExternalCallAction, Request> fromProbe, IProbe<ExternalCallAction, Request> toProbe) {
		fromProbe.enableCaching();
		toProbe.forEachMeasurement(measurement -> {
			notify(calculate(fromProbe.getLastMeasurementOf(measurement.getWho()), measurement));
		});
	}

	@Override
	public Measurement<Pair<ExternalCallAction, ExternalCallAction>, Request> calculate(
			Measurement<ExternalCallAction, Request> from, Measurement<ExternalCallAction, Request> to) {
		if (from == null) {
			return null;
		}

		double when = to.getWhen();
		double responseTime = to.getValue() - from.getValue();

		return new Measurement<Pair<ExternalCallAction, ExternalCallAction>, Request>("TIME_SPAN",
				new MeasuringPointPair<>(from.getWhere().getElement(), to.getWhere().getElement(), "responsetime",
						to.getWhere().getContexts()),
				to.getWho(), responseTime, when);
	}

}

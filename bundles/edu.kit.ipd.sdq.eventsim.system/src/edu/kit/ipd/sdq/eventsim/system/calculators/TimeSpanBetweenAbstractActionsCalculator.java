package edu.kit.ipd.sdq.eventsim.system.calculators;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPointPair;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Calculator;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.ProbePair;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.AbstractBinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

@Calculator(metric = "timespan_between_actions", type = Pair.class, fromType = AbstractAction.class, toType = AbstractAction.class, intendedProbes = {
		@ProbePair(from = "before", to = "after") })
public class TimeSpanBetweenAbstractActionsCalculator extends AbstractBinaryCalculator<AbstractAction, AbstractAction> {

	private static final Logger log = Logger.getLogger(TimeSpanBetweenAbstractActionsCalculator.class);

	@Override
	public void setup(IProbe<AbstractAction> fromProbe, IProbe<AbstractAction> toProbe) {
		// if(fromProbe == null || toProbe == null) {
		// log.warn("Cancelled setup of %s because one of the probes supplied is null.");
		// }
		fromProbe.enableCaching();
		toProbe.forEachMeasurement(m -> {
			// find "from"-measurement
			Request request = (Request) m.getWho();
			Measurement<AbstractAction> fromMeasurement = null;
			do {
				fromMeasurement = fromProbe.getLastMeasurementOf(request);
				request = request.getParent();
			} while (fromMeasurement == null && request != null);

			if (fromMeasurement != null) {
				notify(calculate(fromMeasurement, m));
			} else {
				// TODO improve warning, give hits on how to resolve this problem
				log.warn(String.format("Could not find last measurement triggered by %s or a parent request. "
						+ "Skipping calculation.", m.getWho()));
			}
		});
	}

	@Override
	public Measurement<Pair<AbstractAction, AbstractAction>> calculate(Measurement<AbstractAction> from,
			Measurement<AbstractAction> to) {
		if (from == null) {
			return null;
		}

		double when = to.getWhen();
		double timeDifference = to.getValue() - from.getValue();
		
		MeasuringPoint<Pair<AbstractAction, AbstractAction>> mp = new MeasuringPointPair<>(from.getWhere(), to.getWhere(), "timespan", to.getWhere().getContexts());
		return new Measurement<>("TIME_SPAN_BETWEEN_ACTIONS_SYSTEM", mp, to.getWho(), timeDifference, when);
	}

}

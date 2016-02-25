package edu.kit.ipd.sdq.eventsim.measurement.calculator;

import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;

public class CalculatorBuilder<F, S, C extends ProbeConfiguration>
		implements IntermediateCalculatorFrom<F, S>, IntermediateCalculatorTo<F, S> {

	private IProbe<F> fromProbe;

	private IProbe<S> toProbe;

	private BinaryCalculator<F, S> c;

	private MeasurementFacade<C> measurementFacade;

	private CalculatorBuilder(BinaryCalculator<F, S> c, MeasurementFacade<C> facade) {
		this.c = c;
		this.measurementFacade = facade;
	}

	@Override
	public IntermediateCalculatorTo<F, S> from(F first, String property, Object... measurementContexts) {
		fromProbe = measurementFacade.createProbe(first, property, measurementContexts);
		return this;
	}

	@Override
	public BinaryCalculator<F, S> to(S second, String property, Object... measurementContexts) {
		toProbe = measurementFacade.createProbe(second, property, measurementContexts);
		c.setup(fromProbe, toProbe);
		return c;
	}

	public static <F, S, C extends ProbeConfiguration> IntermediateCalculatorFrom<F, S> create(BinaryCalculator<F, S> c,
			MeasurementFacade<C> measurementFacade) {
		return new CalculatorBuilder<F, S, C>(c, measurementFacade);
	}

}

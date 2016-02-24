package edu.kit.ipd.sdq.eventsim.instrumentation.builder;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;

public interface RestrictionBuilder<I extends Instrumentable, P> {
	
	RestrictionBuilder<I, P> underRestriction(InstrumentableRestriction<I> restriction);
	
	ProbeAndCalculatorBuilder<P> addProbe(String measuredProperty);

	CalculatorRepBuilderFrom<P> addCalculator(String metric);

	InstrumentationDescriptionBuilder ruleDone();
	
}

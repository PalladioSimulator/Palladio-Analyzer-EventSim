package edu.kit.ipd.sdq.eventsim.instrumentation.builder;

public interface ProbeAndCalculatorBuilder<P> {

	ProbeAndCalculatorBuilder<P> addProbe(String measuredProperty);

	CalculatorRepBuilderFrom<P> addCalculator(String metric);

	InstrumentationDescriptionBuilder ruleDone();

}

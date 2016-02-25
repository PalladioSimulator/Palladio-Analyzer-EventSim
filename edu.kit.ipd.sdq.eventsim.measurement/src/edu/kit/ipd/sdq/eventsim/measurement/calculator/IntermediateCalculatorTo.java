package edu.kit.ipd.sdq.eventsim.measurement.calculator;

/**
 * A half-finished {@link BinaryCalculator} that expects to be provided with the second probe ("to"-probe) in this
 * construction step.
 * 
 * @author Philipp Merkle
 *
 * @param <R>
 * @param <F>
 * @param <S>
 * @param <T>
 */
public interface IntermediateCalculatorTo<F, S> {

	BinaryCalculator<F, S> to(S second, String property, Object... measurementContexts);

}

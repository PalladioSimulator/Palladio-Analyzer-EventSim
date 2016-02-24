package edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;

/**
 * Wraps a {@link CalculatorRepresentative} and adds the ability to select or
 * unselect it.
 * 
 * @author Henning Schulz
 *
 * @param <F>
 *            the calculator from type
 * @param <T>
 *            the calculator to type
 */
public class SelectableCalculator<F, T> {

	private final CalculatorRepresentative<F, T> calculator;

	private boolean selected = false;

	public SelectableCalculator(CalculatorRepresentative<F, T> calculator) {
		this.calculator = calculator;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public CalculatorRepresentative<F, T> getCalculator() {
		return calculator;
	}

}

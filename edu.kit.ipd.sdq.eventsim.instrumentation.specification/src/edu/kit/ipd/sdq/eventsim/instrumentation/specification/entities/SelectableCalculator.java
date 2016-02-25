package edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;

/**
 * Wraps a {@link CalculatorRepresentative} and adds the ability to select or
 * unselect it.
 * 
 * @author Henning Schulz
 *
 */
public class SelectableCalculator {

	private final CalculatorRepresentative calculator;

	private boolean selected = false;

	public SelectableCalculator(CalculatorRepresentative calculator) {
		this.calculator = calculator;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public CalculatorRepresentative getCalculator() {
		return calculator;
	}

}

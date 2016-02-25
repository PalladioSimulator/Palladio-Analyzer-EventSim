package edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;

/**
 * Wraps a {@link ProbeRepresentative} and adds the ability to select or
 * unselect it.
 * 
 * @author Henning Schulz
 *
 */
public class SelectableProbe {

	private final ProbeRepresentative probe;

	private boolean selected = false;

	public SelectableProbe(ProbeRepresentative probe) {
		this.probe = probe;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public ProbeRepresentative getProbe() {
		return probe;
	}

}

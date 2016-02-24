package edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;

/**
 * Wraps a {@link ProbeRepresentative} and adds the ability to select or
 * unselect it.
 * 
 * @author Henning Schulz
 *
 * @param
 * 			<P>
 *            the probe type
 */
public class SelectableProbe<P> {

	private final ProbeRepresentative<P> probe;

	private boolean selected = false;

	public SelectableProbe(ProbeRepresentative<P> probe) {
		this.probe = probe;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public ProbeRepresentative<P> getProbe() {
		return probe;
	}

}

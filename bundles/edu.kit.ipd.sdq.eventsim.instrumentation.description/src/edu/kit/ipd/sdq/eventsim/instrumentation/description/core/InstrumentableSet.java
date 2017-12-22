package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * A set for {@link Instrumentable}s. It is specified indirectly by all entities
 * of a specific type which are not excluded by one of the restrictions. The
 * restrictions are combined with the logical AND. For example, if the
 * restrictions {@code a} and {@code b} are specified, an instrumentable
 * {@code i} is included (meaning not excluded), if the following condition
 * holds:<br>
 * 
 * {@code !a.exclude(i) && !b.exclude(i)}
 * 
 * @author Henning Schulz
 *
 * @param <I>
 *            the most general type of {@link Instrumentable}s the set deals
 *            with.
 */
public abstract class InstrumentableSet<I extends Instrumentable> {

	private List<InstrumentableRestriction<I>> restrictions = new ArrayList<>();

	@XmlElementWrapper(name = "restrictions")
	@XmlElement(name = "restriction")
	public List<InstrumentableRestriction<I>> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<InstrumentableRestriction<I>> restrictions) {
		this.restrictions = restrictions;
	}

	public void addRestriction(InstrumentableRestriction<I> res) {
		restrictions.add(res);
	}

	public void removeRestriction(InstrumentableRestriction<I> res) {
		restrictions.remove(res);
	}

	public boolean contains(I instrumentable) {
		if (restrictions.isEmpty()) {
			return true;
		}

		boolean excluded = restrictions.stream().map(res -> res.exclude(instrumentable)).reduce((a, b) -> a || b).get();
		return !excluded;
	}

	public List<I> filter(List<I> instrumentables) {
		List<I> filtered = instrumentables;

		for (InstrumentableRestriction<I> res : restrictions) {
			filtered = res.filter(filtered);
		}

		return filtered;
	}

}

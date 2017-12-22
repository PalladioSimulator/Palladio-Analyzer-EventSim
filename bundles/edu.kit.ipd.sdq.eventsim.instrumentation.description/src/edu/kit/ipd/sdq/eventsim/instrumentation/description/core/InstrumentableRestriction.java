package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionXmlAdapter;

/**
 * Represents a restriction of the totality of all elements of a type {@code I}
 * to all of them not excluded by the implementation of the
 * {@link InstrumentableRestriction#exclude(Instrumentable) exclude()} method.
 * <br>
 * 
 * Note that every class implementing this interface must be listed in the
 * {@code @XmlElements} annotation of
 * {@link InstrumentableSet#getRestrictions()}!
 * 
 * @author Henning Schulz
 *
 * @param <I>
 *            the type of {@link Instrumentable} the restriction deals with
 */
@XmlJavaTypeAdapter(RestrictionXmlAdapter.class)
public interface InstrumentableRestriction<I extends Instrumentable> {

	public static final String EXTENSION_POINT_ID = "edu.kit.ipd.sdq.eventsim.instrumentation.description.restrictions";

	default List<I> filter(List<I> actions) {
		return actions.stream().filter(a -> !exclude(a)).collect(Collectors.toList());
	}

	/**
	 * Returns if an instrumentable is excluded.
	 * 
	 * @param instrumentable
	 *            the instrumentable to be checked
	 * @return {@code true}, if the instrumentable should be excluded or
	 *         {@code false} otherwise
	 */
	boolean exclude(I instrumentable);

	String getHint();

}

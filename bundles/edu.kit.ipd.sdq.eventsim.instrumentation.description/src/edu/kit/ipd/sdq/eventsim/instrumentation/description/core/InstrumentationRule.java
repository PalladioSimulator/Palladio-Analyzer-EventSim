package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

/**
 * A common interface for instrumentation rules. The name can be used for
 * visualization and need not be unique.
 * 
 * @author Henning Schulz
 *
 */
public interface InstrumentationRule {

	String getName();

	void setName(String name);

	/**
	 * Returns if an {@link Instrumentable} would be probed if it comes to
	 * injection of the instrumentation.
	 * 
	 * @param instrumentable
	 *            an {@link Instrumentable} to be checked
	 * @return {@code true}, if the instrumentable will be probed or
	 *         {@code false} otherwise
	 */
	boolean affects(Instrumentable instrumentable);

}

package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;

/**
 * Responsible for injecting probes and calculators. It holds an
 * {@link InstrumentationDescription} to decide in which way an element is to be
 * instrumented.
 * 
 * @author Henning Schulz
 *
 * @param <E>
 *            the type of elements this instrumentor can deal with
 * @param <C>
 *            the type of the {@link ProbeConfiguration}
 */
public interface Instrumentor<E, C extends ProbeConfiguration> {

	void instrument(E element);

	/**
	 * Instruments all elements of type {@code E} based on the held
	 * instrumentation description if possible. Note that some instrumentors
	 * might not implement this method since they do not know any elements.
	 */
	void instrumentAll();

}

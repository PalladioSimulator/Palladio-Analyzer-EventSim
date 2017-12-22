package edu.kit.ipd.sdq.eventsim.measurement;

/**
 * A listener that is notified whenever a new {@link Measurement} has been produced.
 * 
 * @author Philipp Merkle
 *
 * @param <E>
 */
public interface MeasurementListener<E> {

	void notify(Measurement<E> m);

}

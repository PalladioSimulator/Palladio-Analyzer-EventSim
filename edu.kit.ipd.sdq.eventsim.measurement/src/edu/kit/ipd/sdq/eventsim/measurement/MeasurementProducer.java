package edu.kit.ipd.sdq.eventsim.measurement;

/**
 * An emitter for {@link Measurement}s.
 * 
 * @author Philipp Merkle
 * 
 */
public interface MeasurementProducer<E> {

	public void forEachMeasurement(MeasurementListener<E> l);
	
}

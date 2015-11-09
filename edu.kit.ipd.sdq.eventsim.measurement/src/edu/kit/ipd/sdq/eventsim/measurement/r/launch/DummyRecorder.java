package edu.kit.ipd.sdq.eventsim.measurement.r.launch;

import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.recorderframework.IRecorder;
import org.palladiosimulator.recorderframework.config.IRecorderConfiguration;

/**
 * Dummy implementation for the {@link IRecorder} interface. Required only because EventSim exploits the recorder
 * framework to contribute a "R configuration" tab to the launch configuration, though EventSim doesn't support the
 * recorder framework actually.
 * <p>
 * EventSim won't call any of these methods, so an exception is thrown for each method invocation to indicate a
 * misconfiguration, e.g. when trying to use R recorder with SimuCom.
 * 
 * @author Philipp Merkle
 *
 */
public class DummyRecorder implements IRecorder {

	@Override
	public void initialize(IRecorderConfiguration recorderConfiguration) {
		throwUnsupportedException();
	}

	@Override
	public void writeData(MeasuringValue measurement) {
		throwUnsupportedException();
	}

	@Override
	public void flush() {
		throwUnsupportedException();
	}

	@Override
	public void newMeasurementAvailable(MeasuringValue newMeasurement) {
		throwUnsupportedException();
	}

	@Override
	public void preUnregister() {
		throwUnsupportedException();
	}

	private void throwUnsupportedException() {
		throw new UnsupportedOperationException("The R Project Recorder can only be used together with EventSim.");
	}

}

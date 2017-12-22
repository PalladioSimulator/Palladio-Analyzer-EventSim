package edu.kit.ipd.sdq.eventsim.measurement.r.launch;

import java.util.Map;

import org.palladiosimulator.recorderframework.config.IRecorderConfiguration;
import org.palladiosimulator.recorderframework.config.IRecorderConfigurationFactory;

/**
 * See {@link DummyRecorder} for an explanation of the existence of this class.
 * 
 * @author Philipp Merkle
 *
 */
public class DummyRecorderConfigurationFactory implements IRecorderConfigurationFactory {

	@Override
	public void initialize(Map<String, Object> configuration) {
		// do nothing
	}

	@Override
	public IRecorderConfiguration createRecorderConfiguration(Map<String, Object> configuration) {
		// do nothing
		return null;
	}

	@Override
	public void finalizeRecorderConfigurationFactory() {
		// do nothing

	}

}

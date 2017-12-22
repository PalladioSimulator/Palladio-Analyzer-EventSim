package edu.kit.ipd.sdq.eventsim.test.util.builder;

import java.util.HashMap;
import java.util.Map;

import de.uka.ipd.sdq.simulation.AbstractSimulationConfig;
import edu.kit.ipd.sdq.eventsim.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;

/**
 * Builds {@link SimulationConfiguration} instances.
 * 
 * @author Philipp Merkle
 */
public class ConfigurationBuilder {

	private static final String UNLIMITED = "-1";

	private final Map<String, Object> configMap;

	private final PCMModel model;

	public ConfigurationBuilder(PCMModel model) {
		configMap = new HashMap<>();
		configMap.put(AbstractSimulationConfig.VERBOSE_LOGGING, false);
		configMap.put(AbstractSimulationConfig.SIMULATION_TIME, UNLIMITED);
		configMap.put(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT, UNLIMITED);
		configMap.put(AbstractSimulationConfig.USE_FIXED_SEED, false);
		configMap.put(AbstractSimulationConfig.PERSISTENCE_RECORDER_NAME, "Rserve Connector for EventSim");
		configMap.put(AbstractSimulationConfig.EXPERIMENT_RUN, "Simulation test run"); // name of the experiment run
		this.model = model;
	}

	public ConfigurationBuilder stopAtSimulationTime(int time) {
		configMap.put(AbstractSimulationConfig.SIMULATION_TIME, new Integer(time).toString());
		return this;
	}

	public ConfigurationBuilder stopAtMeasurementCount(int count) {
		configMap.put(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT, new Integer(count).toString());
		return this;
	}

	public SimulationConfiguration build() {
		if (configMap.get(AbstractSimulationConfig.SIMULATION_TIME).equals(UNLIMITED)
				&& configMap.get(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT).equals(UNLIMITED)) {
			throw new RuntimeException("Require at least one stopping criterion");
		}
		SimulationConfiguration config = new SimulationConfiguration(configMap, false);
		config.setModel(model);
		return config;
	}

}

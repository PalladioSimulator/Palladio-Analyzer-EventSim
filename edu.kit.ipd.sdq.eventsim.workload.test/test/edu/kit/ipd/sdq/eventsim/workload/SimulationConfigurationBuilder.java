package edu.kit.ipd.sdq.eventsim.workload;

import java.util.HashMap;
import java.util.Map;

import de.uka.ipd.sdq.simulation.AbstractSimulationConfig;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;

public class SimulationConfigurationBuilder {

	private Map<String, Object> configMap;

	private PCMModel model;

	public SimulationConfigurationBuilder(PCMModel model) {
		configMap = new HashMap<>();
		configMap.put(AbstractSimulationConfig.VERBOSE_LOGGING, false);
		configMap.put(AbstractSimulationConfig.SIMULATION_TIME, "-1");
		configMap.put(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT, "-1");
		configMap.put(AbstractSimulationConfig.USE_FIXED_SEED, false);
		configMap.put(AbstractSimulationConfig.PERSISTENCE_RECORDER_NAME, "Rserve Connector for EventSim");
		this.model = model;
	}

	public SimulationConfigurationBuilder stopAtSimulationTime(int time) {
		configMap.put(AbstractSimulationConfig.SIMULATION_TIME, new Integer(time).toString());
		return this;
	}

	public SimulationConfigurationBuilder stopAtMeasurementCount(int count) {
		configMap.put(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT, new Integer(count).toString());
		return this;
	}

	public SimulationConfiguration buildConfiguration() {
		SimulationConfiguration config = new SimulationConfiguration(configMap, false);
		config.setModel(model);
		return config;
	}

}

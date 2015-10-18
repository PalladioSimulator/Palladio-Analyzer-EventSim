package edu.kit.ipd.sdq.eventsim.middleware.simulation.config;

import java.util.Map;

import org.palladiosimulator.analyzer.workflow.ConstantsContainer;

import de.uka.ipd.sdq.simulation.AbstractSimulationConfig;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.PCMModel;

/**
 * A configuration object for a simulation component based advanced simulator
 * configuration.
 * 
 * This objects provides all properties necessary to be able to build a
 * {@link EventSimConfig} base on it.
 * 
 * @author Christoph Föhrdes
 */
public class SimulationConfiguration extends AbstractSimulationConfig implements ISimulationConfiguration {

	public static String SIMULATION_COMPONENT_SIMULATOR_ID = "de.uka.ipd.sdq.codegen.simucontroller.simcomp";

	public static String CONFIG_KEY_SIMULATION_COMPONENTS_CONFIG = "simCompConfig";

	private static final long serialVersionUID = 7117529282079662258L;

	private Map<String, Object> configMap;

	// TODO obsolete?
	private final String usageModelFile;
	private final String allocationModelFile;
	
    private PCMModel model;

	public SimulationConfiguration(Map<String, Object> configuration, boolean debug) {
		super(configuration, debug);
		this.configMap = configuration;
		try {
			this.usageModelFile = (String) configuration.get(ConstantsContainer.USAGE_FILE);
			this.allocationModelFile = (String) configuration.get(ConstantsContainer.ALLOCATION_FILE);
		} catch (final Exception e) {
			throw new RuntimeException("Setting up properties failed, please check launch config (check all tabs).", e);
		}
	}

	@Override
	public String getUsageModelFile() {
		return this.usageModelFile;
	}

	@Override
	public String getAllocationModelFile() {
		return this.allocationModelFile;
	}

	@Override
	public Map<String, Object> getConfigurationMap() {
		return this.configMap;
	}

	@Override
	public long[] getRandomSeed() {
		return super.randomSeed;
	}
	
	public PCMModel getPCMModel() {
		return model;
	}

	public void setModel(PCMModel model) {
		this.model = model;
	}
	
}

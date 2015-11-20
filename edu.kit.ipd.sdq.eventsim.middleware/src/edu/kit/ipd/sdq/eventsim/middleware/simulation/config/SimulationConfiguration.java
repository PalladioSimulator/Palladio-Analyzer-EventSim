package edu.kit.ipd.sdq.eventsim.middleware.simulation.config;

import java.util.Map;

import org.palladiosimulator.analyzer.workflow.ConstantsContainer;

import de.uka.ipd.sdq.simulation.AbstractSimulationConfig;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;

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

	private static final long serialVersionUID = 8569962148603263000L;

	private Map<String, Object> configMap;
	
    private PCMModel model;

	public SimulationConfiguration(Map<String, Object> configuration, boolean debug) {
		super(configuration, debug);
		this.configMap = configuration;
	}

	@Override
	public Map<String, Object> getConfigurationMap() {
		return this.configMap;
	}

	@Override
	public long[] getRandomSeed() {
		return super.randomSeed;
	}
	
	@Override
	public PCMModel getPCMModel() {
		return model;
	}

	public void setModel(PCMModel model) {
		this.model = model;
	}
	
}

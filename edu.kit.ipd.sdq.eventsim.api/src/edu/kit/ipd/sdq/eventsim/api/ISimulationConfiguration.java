package edu.kit.ipd.sdq.eventsim.api;

import java.util.List;
import java.util.Map;

import de.uka.ipd.sdq.simulation.ISimulationListener;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;

/**
 * Represents a configuration for a simulation component based simulation.
 * 
 * @author Christoph Föhrdes
 */
public interface ISimulationConfiguration extends ISimulationConfig {
    
	/**
	 * Gives access to the raw configuration map as created at simulation launch.
	 * 
	 * @return A map of configuration keys and values
	 */
	Map<String, Object> getConfigurationMap();

	/**
	 * Indicates if the simulation is in debug mode or not.
	 * 
	 * @return true if in debug or false if not.
	 */
	boolean isDebug();

	long[] getRandomSeed();

	PCMModel getPCMModel();
	
	long getSimuTime();
	
	long getMaxMeasurementsCount();
	
	List<ISimulationListener> getListeners();

    boolean isSimulateLinkingResources();

    boolean isSimulateThroughputOfLinkingResources();

}

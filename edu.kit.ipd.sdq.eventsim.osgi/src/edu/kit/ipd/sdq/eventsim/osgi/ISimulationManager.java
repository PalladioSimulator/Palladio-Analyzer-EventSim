package edu.kit.ipd.sdq.eventsim.osgi;

import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;

public interface ISimulationManager {

	public int prepareSimulation(SimulationConfiguration config);
	
	public ISimulationMiddleware getMiddleware(int simulationId);

	void disposeSimulation(int simulationId);
	
}

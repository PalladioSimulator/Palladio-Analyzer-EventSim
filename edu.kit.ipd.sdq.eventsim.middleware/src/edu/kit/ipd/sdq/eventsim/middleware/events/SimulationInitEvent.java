package edu.kit.ipd.sdq.eventsim.middleware.events;

import edu.kit.ipd.sdq.eventsim.middleware.ISimulationConfiguration;

public class SimulationInitEvent extends AbstractSimulationEvent {

	private ISimulationConfiguration simulationConfiguration;

	public SimulationInitEvent(ISimulationConfiguration simulationConfiguration) {
		this.simulationConfiguration = simulationConfiguration;
	}

	public ISimulationConfiguration getSimulationConfiguration() {
		return simulationConfiguration;
	}

}

package edu.kit.ipd.sdq.eventsim.middleware.events;

import edu.kit.ipd.sdq.eventsim.middleware.ISimulationConfiguration;

public class SimulationInitEvent extends SimulationEvent {

	public static final String EVENT_ID = SimulationEvent.ID_PREFIX + "middleware/SIMULATION_INIT";

	private ISimulationConfiguration simulationConfiguration;

	public SimulationInitEvent(ISimulationConfiguration simulationConfiguration) {
		super(SimulationInitEvent.EVENT_ID);
		this.simulationConfiguration = simulationConfiguration;
	}

	public ISimulationConfiguration getSimulationConfiguration() {
		return simulationConfiguration;
	}

}

package edu.kit.ipd.sdq.eventsim.middleware.events;

public class SimulationFinalizeEvent extends SimulationEvent {

	public static final String EVENT_ID = SimulationEvent.ID_PREFIX + "middleware/SIMULATION_FINALIZE";

	public SimulationFinalizeEvent() {
		super(SimulationFinalizeEvent.EVENT_ID);
	}

}

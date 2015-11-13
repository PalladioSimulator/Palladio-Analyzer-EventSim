package edu.kit.ipd.sdq.eventsim.middleware.events;

/**
 * A simulation event triggered by a simulation component.
 * 
 * @author Christoph Föhrdes
 */
public abstract class SimulationEvent {

	/**
	 * Prefix for an event ID to identify simulation component events.
	 */
	public static String ID_PREFIX = "simcomp/";

	public static String SIMCOMP_EVENT_PROPERTY = "simcomp.event";

	private String eventId;

	public SimulationEvent(String eventId) {
		this.eventId = eventId;
	}

	/**
	 * The unique ID for a specific event.
	 * 
	 * @return A unique event ID
	 */
	public String getEventId() {
		return this.eventId;
	}

}

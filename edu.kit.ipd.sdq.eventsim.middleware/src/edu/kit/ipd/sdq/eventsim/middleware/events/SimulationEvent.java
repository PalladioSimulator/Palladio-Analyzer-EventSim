package edu.kit.ipd.sdq.eventsim.middleware.events;

/**
 * A simulation event triggered or handled by a simulation component.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
public interface SimulationEvent {

	/**
	 * property name indicating the {@code SimulationEvent} encapsulated by the OSGi event to be used as key in a
	 * properties map
	 */
	public static String ENCAPSULATED_EVENT = "encapsulated.event";

	/**
	 * @return the unique topic name of the given event type.
	 */
	public static String topicName(Class<? extends SimulationEvent> eventType) {
		return eventType.getName().replaceAll("\\.", "/");
	}

}

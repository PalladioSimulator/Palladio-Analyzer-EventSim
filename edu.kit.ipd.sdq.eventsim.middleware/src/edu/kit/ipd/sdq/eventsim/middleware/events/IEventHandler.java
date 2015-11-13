package edu.kit.ipd.sdq.eventsim.middleware.events;

/**
 * A handler callback for a simulation component event.
 * 
 * @author Christoph Föhrdes
 */
public interface IEventHandler <T extends SimulationEvent> {

	/**
	 * The handle method called when a specific event was triggered.
	 * 
	 * @param simulationEvent
	 *            The triggered event
	 * @param simulationEvent
	 *            The triggered event
	 */
	void handle(T simulationEvent);
}

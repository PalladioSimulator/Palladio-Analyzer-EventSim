package edu.kit.ipd.sdq.eventsim.api.events;

/**
 * A handler callback for a simulation component event.
 * 
 * @author Christoph Föhrdes
 */
public interface IEventHandler<T extends SimulationEvent> {

    public enum Registration {
        KEEP_REGISTERED, UNREGISTER;
    }

    /**
     * The handle method called when a specific event was triggered.
     * 
     * @param simulationEvent
     *            The triggered event
     * @param simulationEvent
     *            The triggered event
     * @return {@code true}, if this handler is not interested in further events and shall
     *         unregistered; {@code false}, else.
     */
    Registration handle(T simulationEvent);
}

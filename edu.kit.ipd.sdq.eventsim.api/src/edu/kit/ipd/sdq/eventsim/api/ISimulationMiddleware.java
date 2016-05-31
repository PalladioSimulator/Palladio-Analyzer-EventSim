package edu.kit.ipd.sdq.eventsim.api;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simulation.IStatusObserver;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.components.events.IEventHandler;
import edu.kit.ipd.sdq.eventsim.components.events.SimulationEvent;

/**
 * Represents the central point of a simulation component based simulation.
 * 
 * @author Christoph Föhrdes
 */
public interface ISimulationMiddleware {
    
	/**
	 * Starts a simulation run.
	 * 
	 * @param statusObserver
	 *            A status observer which indicates the simulation progress in
	 *            the GUI.
	 */
	void startSimulation(final IStatusObserver statusObserver);

	/**
	 * Stops the simulation run.
	 */
	void stopSimulation();	

	/**
	 * Gives access to the simulation configuration provided by the user on
	 * launch of the simulation run.
	 * 
	 * @return A simulation configuration
	 */
	ISimulationConfiguration getSimulationConfiguration();

	/**
	 * Gives access to the abstract sim engine simulation model, which is the
	 * core of the simulation. It basically represents the main simulation
	 * timeline.
	 * 
	 * @return The simulation model
	 */
	ISimulationModel getSimulationModel();

	/**
	 * Gives access to the simulation control instance
	 * 
	 * @return The simulation control
	 */
	ISimulationControl getSimulationControl();

	/**
	 * Triggers the specified simulation event. Does not return to the caller until delivery of the event is completed.
	 * 
	 * @param event
	 *            the event to trigger
	 */
	void triggerEvent(SimulationEvent event);

	/**
	 * Registers a new event handler for events of a specified type.
	 * 
	 * @param eventType
	 *            The event type to listen for
	 * @param handler
	 *            The event handler
	 */
	<T extends SimulationEvent> void registerEventHandler(Class<T> eventType, final IEventHandler<T> handler);

	<T extends SimulationEvent> void registerEventHandler(Class<T> eventType, final IEventHandler<T> handler, String filter);
	
	/**
	 * Gives access the the amount of measurements done in the current
	 * simulation. One measurement means one user request was entirely
	 * processed.
	 * 
	 * @return The amount of measurement for the current simulation run.
	 */
	int getMeasurementCount();

	/**
	 * Increases the simulation measurement count. One measurement means one
	 * user request was entirely processed.
	 */
	void increaseMeasurementCount();

	IRandomGenerator getRandomGenerator();

}

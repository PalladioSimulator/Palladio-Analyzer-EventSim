package edu.kit.ipd.sdq.eventsim.middleware;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simulation.IStatusObserver;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.middleware.events.IEventHandler;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.PCMModel;

/**
 * Represents the central point of a simulation component based simulation.
 * 
 * @author Christoph Föhrdes
 */
public interface ISimulationMiddleware {

	/**
	 * Initializes the middleware with a simulation configuration provided by
	 * the user on launch. Called in the prepare simulation phase.
	 * 
	 * @param configuration
	 *            A simulation configuration
	 * @param pcmModel
	 *            The PCM model to be simulated
	 */
	void initialize(ISimulationConfiguration config, PCMModel pcmModel);

	/**
	 * Starts a simulation component based simulation.
	 * 
	 * @param statusObserver
	 *            A status observer which indicates the simulation progress in
	 *            the GUI.
	 */
	void startSimulation(final IStatusObserver statusObserver);

	/**
	 * Stops a simulation run simulation.
	 */
	void stopSimulation();

//	/**
//	 * Returns a simulation component out of a list of alternatives based on the
//	 * simulation configuration and a simulation context.
//	 * 
//	 * @param requestingType
//	 *            The component type requesting access to a required simulation
//	 *            component. For example ISystem when accessing a
//	 *            IActiveResource from ISystem.
//	 * @param requiredType
//	 *            The component type to be accessed. For example IActiveResource
//	 *            when accessing a IActiveResource from ISystem.
//	 * @param componentList
//	 *            A list of alternative simulation components to select one
//	 *            from. For example a list of IActiveResource implementations
//	 *            when accessing a IActiveResource from ISystem.
//	 * @param context
//	 *            The simulation context used to determine the simulation
//	 *            component
//	 * 
//	 * @return The simulation component to use
//	 */
//	ISimulationComponent getSimulationComponent(Class<? extends ISimulationComponent> requestingType, Class<? extends ISimulationComponent> requiredType, List<? extends ISimulationComponent> componentList, AbstractSimulationContext context);

	/**
	 * Gives access to the simulation configuration provided by the user on
	 * launch of the simulation run.
	 * 
	 * @return A simulation configuration
	 */
	ISimulationConfiguration getSimulationConfiguration();

	/**
	 * Gives access to the PCM model to be simulated.
	 * 
	 * @return The PCM model to be simulated
	 */
	PCMModel getPCMModel();

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
	 * Triggers a specific simulation event. All registered event handlers are
	 * processed synchronously.
	 * 
	 * @param event
	 *            The event type to trigger
	 */
	void triggerEvent(SimulationEvent event);

	/**
	 * Registers a new event handler for a specific event. This event handler is
	 * not unregistered on cleanup of the simulation middleware.
	 * 
	 * @param eventId
	 *            The event type to listen for
	 * @param handler
	 *            The event handler callback.
	 */
	<T extends SimulationEvent> void registerEventHandler(String eventId, IEventHandler<T> handler);

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
	
	MeasurementStorage getMeasurementStorage();

}

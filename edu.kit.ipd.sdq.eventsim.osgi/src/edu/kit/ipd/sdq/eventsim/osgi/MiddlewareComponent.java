package edu.kit.ipd.sdq.eventsim.osgi;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simulation.IStatusObserver;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.events.IEventHandler;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.PCMModel;

@Component(factory = "middleware.factory")
public class MiddlewareComponent implements ISimulationMiddleware {

	private static final Logger log = Logger.getLogger(MiddlewareComponent.class);

	private ISimulationMiddleware middlewareDelegate;

	private int simulationId;

	@Activate
	public void activate(ComponentContext ctx) {
		simulationId = (int) ctx.getProperties().get(SimulationManager.SIMULATION_ID);

		middlewareDelegate = new SimulationMiddleware();
	}

	@Deactivate
	public void deactivate(ComponentContext ctx) {
		/*
		 * TODO currently the following statement is necessary to prevent memory leaks. However, this indicates that 
		 * references to this component are not released properly, hence preventing the GC from garbage collecting this
		 * instance.
		 */
		middlewareDelegate = null;
		log.debug("Deactivated simulation middleware component (Simulation ID = " + simulationId + ")");
	}

	public int getSimulationId() {
		return simulationId;
	}

	public void initialize(ISimulationConfiguration config, PCMModel pcmModel) {
		middlewareDelegate.initialize(config, pcmModel);
	}

	public void startSimulation(IStatusObserver statusObserver) {
		middlewareDelegate.startSimulation(statusObserver);
	}

	public void stopSimulation() {
		middlewareDelegate.stopSimulation();
	}

	public ISimulationConfiguration getSimulationConfiguration() {
		return middlewareDelegate.getSimulationConfiguration();
	}

	public PCMModel getPCMModel() {
		return middlewareDelegate.getPCMModel();
	}

	public ISimulationModel getSimulationModel() {
		return middlewareDelegate.getSimulationModel();
	}

	public ISimulationControl getSimulationControl() {
		return middlewareDelegate.getSimulationControl();
	}

	public void triggerEvent(SimulationEvent event) {
		middlewareDelegate.triggerEvent(event);
	}

	public <T extends SimulationEvent> void registerEventHandler(String eventId, IEventHandler<T> handler) {
		middlewareDelegate.registerEventHandler(eventId, handler);
	}

	public int getMeasurementCount() {
		return middlewareDelegate.getMeasurementCount();
	}

	public void increaseMeasurementCount() {
		middlewareDelegate.increaseMeasurementCount();
	}

	public IRandomGenerator getRandomGenerator() {
		return middlewareDelegate.getRandomGenerator();
	}

	public MeasurementStorage getMeasurementStorage() {
		return middlewareDelegate.getMeasurementStorage();
	}

}

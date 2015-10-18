package edu.kit.ipd.sdq.eventsim.osgi;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simulation.IStatusObserver;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.events.IEventHandler;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.PCMModel;

@Component(factory = "middleware.factory")
public class MiddlewareComponent implements ISimulationMiddleware {
	
	private ISimulationMiddleware middlewareDelegate;
	
	private int simulationId;

	@Activate
	void activate(ComponentContext ctx) {
		simulationId = (int) ctx.getProperties().get(SimulationManager.SIMULATION_ID);
		
		middlewareDelegate = new SimulationMiddleware();
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

	public void registerEventHandler(String eventId, IEventHandler<? extends SimulationEvent> handler) {
		middlewareDelegate.registerEventHandler(eventId, handler);
	}

	public void registerEventHandler(String eventId, IEventHandler<? extends SimulationEvent> handler,
			boolean unregisterOnReset) {
		middlewareDelegate.registerEventHandler(eventId, handler, unregisterOnReset);
	}

	public int getMeasurementCount() {
		return middlewareDelegate.getMeasurementCount();
	}

	public void increaseMeasurementCount() {
		middlewareDelegate.increaseMeasurementCount();
	}

	public void resetMeasurementCount() {
		middlewareDelegate.resetMeasurementCount();
	}

	public void reset() {
		middlewareDelegate.reset();
	}

	public IRandomGenerator getRandomGenerator() {
		return middlewareDelegate.getRandomGenerator();
	}

	public RMeasurementStore getMeasurementStore() {
		return middlewareDelegate.getMeasurementStore();
	}

}

package edu.kit.ipd.sdq.eventsim.workload;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.api.events.WorkloadUserFinishedEvent;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationInitEvent;

/**
 * An EventSim based workload simulation component implementation.
 * 
 * @author Christoph Föhrdes
 */
public class EventSimWorkload implements IWorkload {

	private static final Logger logger = Logger.getLogger(EventSimWorkload.class);

	private ISimulationMiddleware middleware;
	private EventSimWorkloadModel model;

	private SystemCallListener systemCallCallback;

	public EventSimWorkload(ISimulationMiddleware middleware) {
		this.middleware = middleware;

		// when the middleware is bound we register for some events
		registerEventHandler();
	}

	private void registerEventHandler() {
		middleware.registerEventHandler(SimulationInitEvent.class, e -> generate());
		middleware.registerEventHandler(SimulationStopEvent.class, e -> finalise());
		middleware.registerEventHandler(WorkloadUserFinishedEvent.class, e -> middleware.increaseMeasurementCount());
	}

	@Override
	public void onSystemCall(SystemCallListener callback) {
		// TODO allow more than one system!?
		systemCallCallback = callback;
	}

	@Override
	public void generate() {
		logger.debug("Generating workload");

		// create the event sim model
		model = new EventSimWorkloadModel(this.middleware, systemCallCallback);

		// launch the event generation
		model.init();
	}

	/**
	 * Cleans up the system simulation component
	 */
	private void finalise() {
		model.finalise();

		/*
		 * TODO should not be required here when instances of this class are released properly (which seems not to be
		 * the case
		 */
		model = null;
	}

}

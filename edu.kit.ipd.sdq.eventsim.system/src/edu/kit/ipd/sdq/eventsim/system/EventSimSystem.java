package edu.kit.ipd.sdq.eventsim.system;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationFinalizeEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationInitEvent;

public class EventSimSystem implements ISystem {

	private static final Logger logger = Logger.getLogger(EventSimSystem.class);

	private ISimulationMiddleware middleware;
	
	private EventSimSystemModel model;
	
	private ActiveResourceListener activeResourceCallback;
	
	private PassiveResourceAcquireListener acquireCallback;
	
	private PassiveResourceReleaseListener releaseCallback;

	public EventSimSystem(ISimulationMiddleware middleware) {
		this.middleware = middleware;
		registerEventHandler();
	}

	private void registerEventHandler() {
		middleware.registerEventHandler(SimulationInitEvent.EVENT_ID, e -> init());
		middleware.registerEventHandler(SimulationFinalizeEvent.EVENT_ID, e -> finalise());
	}

	private void init() {
		model = new EventSimSystemModel(middleware, activeResourceCallback, acquireCallback, releaseCallback);
		model.init();
	}
	
	private void finalise() {
		model.finalise();
		model = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void callService(IUser user, EntryLevelSystemCall call) {
		if (logger.isDebugEnabled()) {
			logger.debug("Received service call from " + user.getId() + " on " + call.getEntityName() + " ("
					+ call.getOperationSignature__EntryLevelSystemCall().getEntityName() + ")");
		}

		// delegate the system call to the event sim model
		model.callService(user, call);

	}

	@Override
	public void onActiveResourceDemand(ActiveResourceListener callback) {
		this.activeResourceCallback = callback;
	}

	@Override
	public void onPassiveResourceAcquire(PassiveResourceAcquireListener callback) {
		this.acquireCallback = callback;
	}

	@Override
	public void onPassiveResourceRelease(PassiveResourceReleaseListener callback) {
		this.releaseCallback = callback;
	}

}

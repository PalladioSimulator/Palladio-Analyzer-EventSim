package edu.kit.ipd.sdq.eventsim.api.events;

import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;

/**
 * Indicates that a {@link IRequest} has been simulated completely.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 * 
 */
public class SystemRequestFinishedEvent extends SimulationEvent {

	public static final String EVENT_ID = SimulationEvent.ID_PREFIX + "system/REQUEST_PROCESSED";

	private IRequest request;

	public SystemRequestFinishedEvent(IRequest request) {
		super(EVENT_ID);
		this.request = request;
	}

	/**
	 * @return the request that has been simulated completely.
	 */
	public IRequest getRequest() {
		return request;
	}

}

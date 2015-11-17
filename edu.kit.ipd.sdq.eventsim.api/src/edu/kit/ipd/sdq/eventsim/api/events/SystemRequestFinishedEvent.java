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
public class SystemRequestFinishedEvent implements SimulationEvent {

	private IRequest request;

	public SystemRequestFinishedEvent(IRequest request) {
		this.request = request;
	}

	/**
	 * @return the request that has been simulated completely.
	 */
	public IRequest getRequest() {
		return request;
	}

}

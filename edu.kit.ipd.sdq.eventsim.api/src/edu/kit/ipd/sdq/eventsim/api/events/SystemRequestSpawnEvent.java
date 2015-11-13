package edu.kit.ipd.sdq.eventsim.api.events;

import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;

/**
 * Indicates that a new {@link IRequest} has been created and waits to be simulated.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 * 
 */
public class SystemRequestSpawnEvent extends SimulationEvent {

	public static final String EVENT_ID = SimulationEvent.ID_PREFIX + "system/REQUEST_START";

	private IRequest request;

	public SystemRequestSpawnEvent(IRequest request) {
		super(EVENT_ID);
		this.request = request;
	}

	/**
	 * @return the newly created request, which is about to be simulated
	 */
	public IRequest getRequest() {
		return request;
	}

}

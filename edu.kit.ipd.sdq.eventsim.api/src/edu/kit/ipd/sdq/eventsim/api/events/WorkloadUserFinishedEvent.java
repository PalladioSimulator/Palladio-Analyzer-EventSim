package edu.kit.ipd.sdq.eventsim.api.events;

import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.middleware.events.AbstractSimulationEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;

/**
 * Indicates that a {@link IUser} has been simulated completely.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 * 
 */
public class WorkloadUserFinishedEvent extends AbstractSimulationEvent {

	private IUser user;

	public WorkloadUserFinishedEvent(IUser user) {
		this.user = user;
	}

	/**
	 * @return the user that has been simulated completely.
	 */
	public IUser getUser() {
		return user;
	}

}

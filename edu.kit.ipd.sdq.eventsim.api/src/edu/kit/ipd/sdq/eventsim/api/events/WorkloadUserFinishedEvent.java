package edu.kit.ipd.sdq.eventsim.api.events;

import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;

/**
 * Indicates that a {@link IUser} has been simulated completely.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 * 
 */
public class WorkloadUserFinishedEvent extends SimulationEvent {

	public static final String EVENT_ID = SimulationEvent.ID_PREFIX + "workload/USER_FINISHED";

	private IUser user;

	public WorkloadUserFinishedEvent(IUser user) {
		super(EVENT_ID);
		this.user = user;
	}

	/**
	 * @return the user that has been simulated completely.
	 */
	public IUser getUser() {
		return user;
	}

}

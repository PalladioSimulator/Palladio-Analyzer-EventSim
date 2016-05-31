package edu.kit.ipd.sdq.eventsim.api.events;

import edu.kit.ipd.sdq.eventsim.api.IUser;

/**
 * Indicates that a new {@link IUser} has been created and waits to be simulated.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 * 
 */
public class WorkloadUserSpawn extends AbstractSimulationEvent {

	private IUser user;

	public WorkloadUserSpawn(IUser user) {
		this.user = user;
	}

	/**
	 * @return the newly created user, which is about to be simulated
	 */
	public IUser getUser() {
		return user;
	}

}

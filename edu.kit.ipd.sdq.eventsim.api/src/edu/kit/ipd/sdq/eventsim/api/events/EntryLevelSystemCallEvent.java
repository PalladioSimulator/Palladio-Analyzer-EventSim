package edu.kit.ipd.sdq.eventsim.api.events;

import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;

public class EntryLevelSystemCallEvent implements SimulationEvent {

	private IUser user;
	
	private EntryLevelSystemCall call;

	public EntryLevelSystemCallEvent(IUser user, EntryLevelSystemCall call) {
		this.user = user;
		this.call = call;
	}
	
	public IUser getUser() {
		return user;
	}
	
	public EntryLevelSystemCall getCall() {
		return call;
	}
	
}

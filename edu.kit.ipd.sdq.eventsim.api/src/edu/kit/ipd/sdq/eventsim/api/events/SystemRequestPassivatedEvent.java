package edu.kit.ipd.sdq.eventsim.api.events;

import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.middleware.events.AbstractSimulationEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;

public class SystemRequestPassivatedEvent extends AbstractSimulationEvent {

	private IRequest request;

	public SystemRequestPassivatedEvent(IRequest request) {
		super();
		this.request = request;
	}
	
	public IRequest getRequest() {
		return request;
	}
	
}

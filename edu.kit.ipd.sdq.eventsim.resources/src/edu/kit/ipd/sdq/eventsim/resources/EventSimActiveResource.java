package edu.kit.ipd.sdq.eventsim.resources;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.components.AbstractComponentFacade;

public class EventSimActiveResource extends AbstractComponentFacade {
	
	private EventSimActiveResourceModel model;

	public EventSimActiveResource() {
		this.model = new EventSimActiveResourceModel(this);
		
		require(ISimulationMiddleware.class, m -> model.init());
		provide(IActiveResource.class, model);
	}
	
}

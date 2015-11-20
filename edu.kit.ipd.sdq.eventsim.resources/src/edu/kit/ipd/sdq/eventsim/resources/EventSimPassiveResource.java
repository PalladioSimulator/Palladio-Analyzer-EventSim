package edu.kit.ipd.sdq.eventsim.resources;

import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.components.AbstractComponentFacade;

public class EventSimPassiveResource extends AbstractComponentFacade {

	private EventSimPassiveResourceModel model;

	public EventSimPassiveResource() {
		this.model = new EventSimPassiveResourceModel(this);
		
		require(ISimulationMiddleware.class, m -> model.init());
		provide(IPassiveResource.class, model);
	}

}

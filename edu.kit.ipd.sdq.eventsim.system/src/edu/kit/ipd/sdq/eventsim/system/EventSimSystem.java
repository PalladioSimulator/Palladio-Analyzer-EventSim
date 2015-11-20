package edu.kit.ipd.sdq.eventsim.system;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.components.AbstractComponentFacade;

public class EventSimSystem extends AbstractComponentFacade {

	private EventSimSystemModel model;

	public EventSimSystem() {
		this.model = new EventSimSystemModel(this);

		require(IActiveResource.class);
		require(IPassiveResource.class);
		require(ISimulationMiddleware.class, m -> model.init());
		provide(ISystem.class, model);
	}

}

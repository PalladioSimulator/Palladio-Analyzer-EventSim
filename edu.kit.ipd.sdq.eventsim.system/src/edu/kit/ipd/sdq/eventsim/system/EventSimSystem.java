package edu.kit.ipd.sdq.eventsim.system;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.components.AbstractComponentFacade;

public class EventSimSystem extends AbstractComponentFacade {

	private static final Logger logger = Logger.getLogger(EventSimSystem.class);

	private EventSimSystemModel model;

	public EventSimSystem() {
		this.model = new EventSimSystemModel(this);

		require(IActiveResource.class);
		require(IPassiveResource.class);
		require(ISimulationMiddleware.class, m -> model.init());
		provide(ISystem.class, model);
	}

}

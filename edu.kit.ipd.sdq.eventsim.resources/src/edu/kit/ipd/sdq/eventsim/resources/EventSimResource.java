package edu.kit.ipd.sdq.eventsim.resources;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.components.AbstractComponentFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;

public class EventSimResource extends AbstractComponentFacade {

	private EventSimActiveResourceModel activeResourceModel;

	private EventSimPassiveResourceModel passiveResourceModel;

	public EventSimResource() {
		this.activeResourceModel = new EventSimActiveResourceModel(this);
		this.passiveResourceModel = new EventSimPassiveResourceModel(this);

		require(ISimulationMiddleware.class, m -> {
			activeResourceModel.init();
			passiveResourceModel.init();
		});
		require(MeasurementStorage.class);
		provide(IActiveResource.class, activeResourceModel);
		provide(IPassiveResource.class, passiveResourceModel);
	}

}

package edu.kit.ipd.sdq.eventsim.resources;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationFinalizeEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationInitEvent;

public class EventSimActiveResource implements IActiveResource {

	private ISimulationMiddleware middleware;
	private EventSimActiveResourceModel model;

	public EventSimActiveResource(ISimulationMiddleware middleware) {
		this.middleware = middleware;
		registerEventHandler();
	}
	
	private void registerEventHandler() {
		middleware.registerEventHandler(SimulationInitEvent.EVENT_ID, e -> init());
		middleware.registerEventHandler(SimulationFinalizeEvent.EVENT_ID, e -> finalise());
	}

	private void init() {
		model = new EventSimActiveResourceModel(middleware);
		model.init();
	}

	private void finalise() {
		model.finalise();
		model = null;
	}

	@Override
	public void consume(IRequest request, ResourceContainer resourceContainer, ResourceType resourceType,
			double absoluteDemand) {
		model.consume(request, resourceContainer, resourceType, absoluteDemand);
	}

}

package edu.kit.ipd.sdq.eventsim.resources;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationInitEvent;

public class EventSimPassiveResource implements IPassiveResource {

	private ISimulationMiddleware middleware;
	private EventSimPassiveResourceModel model;

	public EventSimPassiveResource(ISimulationMiddleware middleware) {
		this.middleware = middleware;
		registerEventHandler();
	}

	private void registerEventHandler() {
		middleware.registerEventHandler(SimulationInitEvent.EVENT_ID, e -> init());
		middleware.registerEventHandler(SimulationStopEvent.EVENT_ID, e -> finalise());
	}

	private void init() {
		model = new EventSimPassiveResourceModel(middleware);
		model.init();
	}

	private void finalise() {
		model.finalise();
		model = null;
	}

	@Override
	public boolean acquire(IRequest request, AssemblyContext ctx, PassiveResource specification, int num) {
		return this.model.acquire(request, ctx, specification, num);
	}

	@Override
	public void release(IRequest request, AssemblyContext ctx, PassiveResource specification, int num) {
		this.model.release(request, ctx, specification, num);
	}

}

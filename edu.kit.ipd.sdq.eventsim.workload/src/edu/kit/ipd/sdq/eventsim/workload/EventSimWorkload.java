package edu.kit.ipd.sdq.eventsim.workload;

import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.components.AbstractComponentFacade;

/**
 * An EventSim based workload simulation component implementation.
 * 
 * @author Christoph Föhrdes
 */
public class EventSimWorkload extends AbstractComponentFacade {

	private EventSimWorkloadModel model;

	public EventSimWorkload() {
		this.model = new EventSimWorkloadModel(this);
		
		require(ISimulationMiddleware.class, m -> model.init());
		require(ISystem.class);
		provide(IWorkload.class, model);
	}

}

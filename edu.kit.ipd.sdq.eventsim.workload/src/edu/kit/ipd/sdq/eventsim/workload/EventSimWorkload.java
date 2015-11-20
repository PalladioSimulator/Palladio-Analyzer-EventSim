package edu.kit.ipd.sdq.eventsim.workload;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.components.AbstractComponentFacade;

/**
 * An EventSim based workload simulation component implementation.
 * 
 * @author Christoph Föhrdes
 */
public class EventSimWorkload extends AbstractComponentFacade {

	private static final Logger logger = Logger.getLogger(EventSimWorkload.class);

	private EventSimWorkloadModel model;

	public EventSimWorkload() {
		this.model = new EventSimWorkloadModel(this);
		
		require(ISimulationMiddleware.class, m -> model.init());
		require(ISystem.class);
		provide(IWorkload.class, model);
	}

}

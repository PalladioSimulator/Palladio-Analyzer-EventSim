package edu.kit.ipd.sdq.eventsim.workload;

import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.components.AbstractComponentFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;

/**
 * An EventSim based workload simulation component implementation.
 * 
 * @author Christoph Föhrdes
 */
public class EventSimWorkload extends AbstractComponentFacade {

	private EventSimWorkloadModel model;

	public EventSimWorkload() {
		this.model = new EventSimWorkloadModel(this);
		
		require(ISystem.class);
		require(ISimulationMiddleware.class, m -> model.init());
		require(MeasurementStorage.class);
		provide(IWorkload.class, model);
	}

}

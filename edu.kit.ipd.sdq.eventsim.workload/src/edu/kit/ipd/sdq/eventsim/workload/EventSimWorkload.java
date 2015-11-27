package edu.kit.ipd.sdq.eventsim.workload;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.IWorkload;

/**
 * An EventSim based workload simulation component implementation.
 * 
 * @author Christoph Föhrdes
 */
public class EventSimWorkload extends AbstractModule {

	@Override
	protected void configure() {
		// bind interfaces of provided services to their implementation
		bind(IWorkload.class).to(EventSimWorkloadModel.class);
	}

}

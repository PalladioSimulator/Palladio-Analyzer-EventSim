package edu.kit.ipd.sdq.eventsim.resources;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;

public class EventSimResource extends AbstractModule {
	
	@Override
	protected void configure() {
		// bind interfaces of provided services to their implementation
		bind(IActiveResource.class).to(EventSimActiveResourceModel.class);
		bind(IPassiveResource.class).to(EventSimPassiveResourceModel.class);
	}

}

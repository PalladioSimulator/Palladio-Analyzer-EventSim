package edu.kit.ipd.sdq.eventsim.api.events;

import java.util.HashMap;
import java.util.Map;

import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.middleware.events.AbstractSimulationEvent;

public class SystemRequestActivatedEvent extends AbstractSimulationEvent {

	public static final String REQUEST_ID = "request.id";
//	public static final String ASSEMBLY_CONTEXT_ID = "assembly.context";
//	public static final String RESOURCE_ID = "resource.id";
	
	private IRequest request;

	public SystemRequestActivatedEvent(IRequest request) {
		super();
		this.request = request;
	}
	
	public IRequest getRequest() {
		return request;
	}
	
	@Override
	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put(REQUEST_ID, Long.toString(request.getId()));
//		properties.put(RESOURCE_ID, passiveResouce.getId());
//		properties.put(ASSEMBLY_CONTEXT_ID, assemblyContext.getId());
		return properties;
	}
	
}

package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

/**
 * Represents an active resource simulation component which can be consumed.
 * 
 * TODO (SimComp) Introduce active resource simulation events
 * 
 * @author Christoph Föhrdes
 * 
 */
public interface IActiveResource {

	/**
	 * Consumes a specific demand of this active resource.
	 * 
	 * @param request
	 * @param resourceContainer
	 * @param resourceType
	 * @param absoluteDemand
	 */
	void consume(IRequest request, ResourceContainer resourceContainer, ResourceType resourceType, double absoluteDemand);

}

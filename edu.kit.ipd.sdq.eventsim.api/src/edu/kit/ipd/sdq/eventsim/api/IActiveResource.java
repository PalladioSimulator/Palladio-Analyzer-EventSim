package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceInterface;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

/**
 * Represents an active resource simulation component which can be consumed.
 * 
 * TODO (SimComp) Introduce active resource simulation events
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
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
    void consume(IRequest request, ResourceContainer resourceContainer, ResourceType resourceType,
            double absoluteDemand, final int resourceServiceID);

    /**
     * Finds and returns the resource type providing the specified resource interface. If multiple
     * resource types provide the specified interface, the result of this method is unspecified, so
     * far.
     * 
     * @param resourceInterface
     *            the resource interface
     * @return the resource type that provides the specified resource interface; {@code null}, if no
     *         such resource type could be found
     */
    ResourceType findResourceType(ResourceInterface resourceInterface);

}

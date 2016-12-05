package edu.kit.ipd.sdq.eventsim.resources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.UnexpectedModelStructureException;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimActiveResource;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

@Singleton
public class ResourceRegistry {

    private static final Logger logger = Logger.getLogger(EventSimActiveResourceModel.class);
    
    // maps (ResourceContainer ID, ResourceType ID) -> SimActiveResource
    private Map<String, SimActiveResource> containerToResourceMap;
    
    private List<Consumer<SimActiveResource>> registrationListeners;
    
    @Inject
    private ResourceFactory resourceFactory;
    
    public ResourceRegistry() {
        containerToResourceMap = new HashMap<>();
        registrationListeners = new LinkedList<>();
    }
    
    
    public void addResourceRegistrationListener(Consumer<SimActiveResource> listener) {
        registrationListeners.add(listener);
    }
    
    private void notifyRegistrationListeners(SimActiveResource resource) {
        registrationListeners.forEach(listener -> listener.accept(resource));
    }
    
    public void finalise() {
        // clean up created resources
        for (edu.kit.ipd.sdq.eventsim.resources.entities.AbstractActiveResource resource : containerToResourceMap
                .values()) {
            resource.deactivateResource();
        }
    }
    
    /**
     * Registers a resource for the specified resource type. Only one resource can be registered for
     * each resource type. Thus, providing a resource for an already registered resource type
     * overwrites the existing resource.
     * 
     * @param type
     *            the type of the resource
     * @param resource
     *            the resource that is to be registered
     */
    private void registerResource(ResourceContainer resourceContainer, ResourceType type, SimActiveResource resource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering a " + type.getEntityName() + " resource at "
                    + PCMEntityHelper.toString(resourceContainer));
        }
        if (this.containerToResourceMap.containsKey(type)) {
            logger.warn("Registered a resource of type " + type.getEntityName()
                    + ", but there was already a resource of this type. The existing resource has been overwritten.");
        }

        // register the created active resource
        this.containerToResourceMap.put(compoundKey(resourceContainer, type), resource);

        notifyRegistrationListeners(resource);
    }

    /**
     * Finds the resource registered for the specified type and resource container, or creates the
     * resource if none is registered. Created resources are added to the registry.
     * 
     * @param resourceContainer
     *            the resource container
     * @param type
     *            the resource type
     * @return the resource of the specified type
     * @throws UnexpectedModelStructureException
     *             if the modeled resource container does not contain a resource specification of
     *             the requested type
     */
    public SimActiveResource findOrCreateResource(ResourceContainer resourceContainer, ResourceType resourceType) {
        if (!containerToResourceMap.containsKey(compoundKey(resourceContainer, resourceType))) {
            ProcessingResourceSpecification foundResourceSpecification = null;
            for (ProcessingResourceSpecification spec : resourceContainer
                    .getActiveResourceSpecifications_ResourceContainer()) {
                if (spec.getActiveResourceType_ActiveResourceSpecification().equals(resourceType)) {
                    foundResourceSpecification = spec;
                    break;
                }
            }
            if (foundResourceSpecification == null) {
                // TODO perhaps support nested resource containers: continue lookup with parent
                String message = String.format("Missing resource type %s for resource container %s.",
                        PCMEntityHelper.toString(resourceType), PCMEntityHelper.toString(resourceContainer));
                throw new UnexpectedModelStructureException(message);
            }

            // create and register the resource
            SimActiveResource resource = resourceFactory.createActiveResource(foundResourceSpecification);
            registerResource(resourceContainer, resourceType, resource);

            logger.info(String.format("Created %s resource with %s scheduling at %s", resourceType.getEntityName(),
                    resource.getSchedulingStrategy().getEntityName(), PCMEntityHelper.toString(resourceContainer)));
        }
        return containerToResourceMap.get(compoundKey(resourceContainer, resourceType));
    }
    
    private String compoundKey(ResourceContainer specification, ResourceType resourceType) {
        return specification.getId() + resourceType.getId();
    }
    
}

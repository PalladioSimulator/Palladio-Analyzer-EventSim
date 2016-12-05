package edu.kit.ipd.sdq.eventsim.resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.palladiosimulator.pcm.core.entity.ResourceProvidedRole;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceInterface;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.scheduler.resources.active.AbstractActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.api.events.IEventHandler.Registration;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationPrepareEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.UnexpectedModelStructureException;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.Instrumentor;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.InstrumentorBuilder;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.BundleProbeLocator;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimActiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimulatedProcess;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

@Singleton
public class EventSimActiveResourceModel implements IActiveResource {

    private static final Logger logger = Logger.getLogger(EventSimActiveResourceModel.class);

    // maps (ResourceContainer ID, ResourceType ID) -> SimActiveResource
    private Map<String, SimActiveResource> containerToResourceMap;

    private Map<ResourceInterface, ResourceType> resourceInterfaceToTypeMap;

    private Instrumentor<SimActiveResource, ?> instrumentor;

    @Inject
    private MeasurementStorage measurementStorage;

    @Inject
    private ISimulationMiddleware middleware;

    @Inject
    private PCMModel pcm;

    private MeasurementFacade<ResourceProbeConfiguration> measurementFacade;

    @Inject
    private InstrumentationDescription instrumentation;

    @Inject
    private ResourceFactory resourceFactory;
    
    @Inject
    private ProcessRegistry processRegistry;

    @Inject
    public EventSimActiveResourceModel(ISimulationMiddleware middleware) {
        // initialize in simulation preparation phase
        middleware.registerEventHandler(SimulationPrepareEvent.class, e -> {
            init();
            return Registration.UNREGISTER;
        });

        containerToResourceMap = new HashMap<>();
        resourceInterfaceToTypeMap = new HashMap<>();
    }

    public void init() {
        // setup measurement facade
        Bundle bundle = Activator.getContext().getBundle();
        measurementFacade = new MeasurementFacade<>(new ResourceProbeConfiguration(), new BundleProbeLocator<>(bundle));

        // create instrumentor for instrumentation description
        instrumentor = InstrumentorBuilder.buildFor(pcm).inBundle(Activator.getContext().getBundle())
                .withDescription(instrumentation).withStorage(measurementStorage).forModelType(ActiveResourceRep.class)
                .withMapping(
                        (SimActiveResource r) -> new ActiveResourceRep(r.getResourceContainer(), r.getResourceType()))
                .createFor(measurementFacade);

        measurementStorage.addIdExtractor(SimActiveResource.class, c -> ((SimActiveResource) c).getId());
        measurementStorage.addNameExtractor(SimActiveResource.class, c -> ((SimActiveResource) c).getName());
        measurementStorage.addIdExtractor(SimulatedProcess.class,
                c -> Long.toString(((SimulatedProcess) c).getEntityId()));
        measurementStorage.addNameExtractor(SimulatedProcess.class, c -> ((SimulatedProcess) c).getName());

        registerEventHandler();
    }

    private void registerEventHandler() {
        middleware.registerEventHandler(SimulationStopEvent.class, e -> {
            finalise();
            return Registration.UNREGISTER;
        });
    }

    @Override
    public void consume(final IRequest request, final ResourceContainer resourceContainer,
            final ResourceType resourceType, final double absoluteDemand, final int resourceServiceID,
            Procedure onServedCallback) {
        final SimActiveResource resource = findOrCreateResource(resourceContainer, resourceType);
        if (resource == null) {
            throw new RuntimeException("Could not find a resource of type " + resourceType.getEntityName());
        }

        resource.consumeResource(processRegistry.getOrCreateSimulatedProcess(request), absoluteDemand, resourceServiceID,
                onServedCallback);
    }

    public void finalise() {
        // clean up created resources
        for (edu.kit.ipd.sdq.eventsim.resources.entities.AbstractActiveResource resource : containerToResourceMap
                .values()) {
            resource.deactivateResource();
        }

        // clean up scheduler
        AbstractActiveResource.cleanProcesses();
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

        // create probes and calculators (if requested by instrumentation description)
        instrumentor.instrument(resource);
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

    @Override
    public ResourceType findResourceType(ResourceInterface resourceInterface) {
        if (!resourceInterfaceToTypeMap.containsKey(resourceInterface)) {
            ResourceRepository resourceRepository = resourceInterface.getResourceRepository__ResourceInterface();
            ResourceType foundType = null;
            for (ResourceType resourceType : resourceRepository.getAvailableResourceTypes_ResourceRepository()) {
                for (ResourceProvidedRole resourceProvidedRole : resourceType
                        .getResourceProvidedRoles__ResourceInterfaceProvidingEntity()) {
                    if (resourceProvidedRole.getProvidedResourceInterface__ResourceProvidedRole().getId()
                            .equals(resourceInterface.getId())) {
                        if (foundType != null) {
                            logger.warn("Found at least two resource types providing the same resource interface. "
                                    + "This case is so far not supported in the simulation. Results may be wrong.");
                        }
                        foundType = resourceType;
                    }
                }
            }
            resourceInterfaceToTypeMap.put(resourceInterface, foundType);
        }
        return resourceInterfaceToTypeMap.get(resourceInterface);
    }

}

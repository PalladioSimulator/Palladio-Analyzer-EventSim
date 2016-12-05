package edu.kit.ipd.sdq.eventsim.resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.palladiosimulator.pcm.core.entity.ResourceProvidedRole;
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
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.Instrumentor;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.InstrumentorBuilder;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.BundleProbeLocator;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimActiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimulatedProcess;

@Singleton
public class EventSimActiveResourceModel implements IActiveResource {

    private static final Logger logger = Logger.getLogger(EventSimActiveResourceModel.class);

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
    private ProcessRegistry processRegistry;

    @Inject
    private ResourceRegistry resourceRegistry;

    @Inject
    public EventSimActiveResourceModel(ISimulationMiddleware middleware) {
        // initialize in simulation preparation phase
        middleware.registerEventHandler(SimulationPrepareEvent.class, e -> {
            init();
            return Registration.UNREGISTER;
        });
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

        resourceRegistry.addResourceRegistrationListener(resource -> {
            // create probes and calculators (if requested by instrumentation description)
            instrumentor.instrument(resource);
        });

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
        final SimActiveResource resource = resourceRegistry.findOrCreateResource(resourceContainer, resourceType);
        if (resource == null) {
            throw new RuntimeException("Could not find a resource of type " + resourceType.getEntityName());
        }

        resource.consumeResource(processRegistry.getOrCreateSimulatedProcess(request), absoluteDemand,
                resourceServiceID, onServedCallback);
    }

    public void finalise() {
        resourceRegistry.finalise();

        // clean up scheduler
        AbstractActiveResource.cleanProcesses();
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

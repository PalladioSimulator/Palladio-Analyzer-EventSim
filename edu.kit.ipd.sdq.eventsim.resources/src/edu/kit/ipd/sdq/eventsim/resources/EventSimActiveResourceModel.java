package edu.kit.ipd.sdq.eventsim.resources;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.osgi.framework.Bundle;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.scheduler.resources.active.AbstractActiveResource;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationPrepareEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.entities.IEntityListener;
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

    // TODO extract class
    private Map<IRequest, SimulatedProcess> requestToSimulatedProcessMap;

    private Instrumentor<SimActiveResource, ?> instrumentor;

    @Inject
    private ISimulationModel model;

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
    public EventSimActiveResourceModel(ISimulationMiddleware middleware) {
        // initialize in simulation preparation phase
        middleware.registerEventHandler(SimulationPrepareEvent.class, e -> init());

        containerToResourceMap = new HashMap<String, SimActiveResource>();
        requestToSimulatedProcessMap = new WeakHashMap<IRequest, SimulatedProcess>();
    }

    public void init() {
        // setup measurement facade
        Bundle bundle = Activator.getContext().getBundle();
        measurementFacade = new MeasurementFacade<>(new ResourceProbeConfiguration(), new BundleProbeLocator<>(bundle));

        // create instrumentor for instrumentation description
        instrumentor = InstrumentorBuilder.buildFor(pcm).inBundle(Activator.getContext().getBundle())
                .withDescription(instrumentation).withStorage(measurementStorage).forModelType(ActiveResourceRep.class)
                .withMapping((SimActiveResource r) -> new ActiveResourceRep(r.getSpecification()))
                .createFor(measurementFacade);

        measurementStorage.addIdExtractor(SimActiveResource.class,
                c -> ((SimActiveResource) c).getSpecification().getId());
        measurementStorage.addNameExtractor(SimActiveResource.class, c -> ((SimActiveResource) c).getName());
        measurementStorage.addIdExtractor(SimulatedProcess.class,
                c -> Long.toString(((SimulatedProcess) c).getEntityId()));
        measurementStorage.addNameExtractor(SimulatedProcess.class, c -> ((SimulatedProcess) c).getName());

        registerEventHandler();
    }

    private void registerEventHandler() {
        middleware.registerEventHandler(SimulationStopEvent.class, e -> finalise());
    }

    @Override
    public void consume(IRequest request, ResourceContainer resourceContainer, ResourceType resourceType,
            double absoluteDemand) {
        final SimActiveResource resource = findOrCreateResource(resourceContainer, resourceType);
        if (resource == null) {
            throw new RuntimeException("Could not find a resource of type " + resourceType.getEntityName());
        }

        resource.consumeResource(getOrCreateSimulatedProcess(request), absoluteDemand);
    }

    public void finalise() {
        // clean up created resources
        for (SimActiveResource resource : containerToResourceMap.values()) {
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

    /**
     * This handler reacts when the Request has been finished and informs the simulated process
     * about that.
     * 
     * @author Philipp Merkle
     */
    private class RequestFinishedHandler implements IEntityListener {

        private WeakReference<SimulatedProcess> process;

        public RequestFinishedHandler(SimulatedProcess process) {
            this.process = new WeakReference<SimulatedProcess>(process);
        }

        @Override
        public void enteredSystem() {
            // nothing to do
        }

        @Override
        public void leftSystem() {
            process.get().terminate();
            requestToSimulatedProcessMap.remove(process.get().getRequest());
        }

    }

    /**
     * Returns the simulated process that is used to schedule resource requests issued by this
     * Request on an active or passive resource.
     * 
     * @return the simulated process
     */
    public SimulatedProcess getOrCreateSimulatedProcess(IRequest request) {
        if (!requestToSimulatedProcessMap.containsKey(request)) {
            SimulatedProcess parent = null;
            if (request.getParent() != null) {
                parent = getOrCreateSimulatedProcess(request.getParent());
            }
            SimulatedProcess process = new SimulatedProcess(model, parent, request);

            // add listener for request finish
            EventSimEntity requestEntity = (EventSimEntity) request;
            requestEntity.addEntityListener(new RequestFinishedHandler(process));

            requestToSimulatedProcessMap.put(request, process);
        }
        return requestToSimulatedProcessMap.get(request);
    }

}

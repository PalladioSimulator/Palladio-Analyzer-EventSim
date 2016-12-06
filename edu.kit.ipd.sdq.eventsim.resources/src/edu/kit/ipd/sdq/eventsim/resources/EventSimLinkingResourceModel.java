package edu.kit.ipd.sdq.eventsim.resources;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.api.ILinkingResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.api.events.IEventHandler.Registration;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationPrepareEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.LinkingResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.Instrumentor;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.InstrumentorBuilder;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.BundleProbeLocator;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimLinkingResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimulatedProcess;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

@Singleton
public class EventSimLinkingResourceModel implements ILinkingResource {

    private static final Logger logger = Logger.getLogger(EventSimLinkingResourceModel.class);

    @Inject
    private LinkingResourceRegistry resourceRegistry;

    @Inject
    private ProcessRegistry processRegistry;

    @Inject
    private MeasurementStorage measurementStorage;

    @Inject
    private InstrumentationDescription instrumentation;

    @Inject
    private PCMModel pcm;

    @Inject
    private ISimulationMiddleware middleware;

    @Inject
    private ISimulationConfiguration configuration;

    @Inject
    public EventSimLinkingResourceModel(ISimulationMiddleware middleware) {
        // initialize in simulation preparation phase
        middleware.registerEventHandler(SimulationPrepareEvent.class, e -> {
            init();
            return Registration.UNREGISTER;
        });
        // finalize on simulation stop
        middleware.registerEventHandler(SimulationStopEvent.class, e -> {
            finalise();
            return Registration.UNREGISTER;
        });
    }

    public void init() {
        // setup measurement facade
        Bundle bundle = Activator.getContext().getBundle();
        MeasurementFacade<ResourceProbeConfiguration> measurementFacade = new MeasurementFacade<>(
                new ResourceProbeConfiguration(), new BundleProbeLocator<>(bundle));

        // add hints for extracting IDs and names
        measurementStorage.addIdExtractor(SimLinkingResource.class, c -> ((SimLinkingResource) c).getId());
        measurementStorage.addNameExtractor(SimLinkingResource.class, c -> ((SimLinkingResource) c).getName());
        measurementStorage.addIdExtractor(SimulatedProcess.class,
                c -> Long.toString(((SimulatedProcess) c).getEntityId()));
        measurementStorage.addNameExtractor(SimulatedProcess.class, c -> ((SimulatedProcess) c).getName());

        // create instrumentor for instrumentation description
        Instrumentor<SimLinkingResource, ?> instrumentor = InstrumentorBuilder.buildFor(pcm)
                .inBundle(Activator.getContext().getBundle()).withDescription(instrumentation)
                .withStorage(measurementStorage).forModelType(LinkingResourceRep.class)
                .withMapping((SimLinkingResource r) -> new LinkingResourceRep(r.getSpecification()))
                .createFor(measurementFacade);

        // instrument newly created resources
        resourceRegistry.addResourceRegistrationListener(resource -> {
            // create probes and calculators (if requested by instrumentation description)
            instrumentor.instrument(resource);
        });

        if (configuration.isSimulateLinkingResources()) {
            logger.warn("Simulation of full middleware marshalling / demarshalling of remote calls is not "
                    + "supported by EventSim. Please change the networking mode in your configuration and "
                    + "restart the simulation.");
            middleware.stopSimulation();
        }

    }

    private void finalise() {
        // TODO finalise registries?
    }

    @Override
    public void consume(IRequest request, LinkingResource specification, double absoluteDemand,
            Procedure onServedCallback) {
        final SimLinkingResource resource = resourceRegistry.findOrCreateResource(specification);
        if (resource == null) {
            throw new RuntimeException(
                    String.format("Could not find linking resource %s", PCMEntityHelper.toString(specification)));
        }

        int resourceServiceID = 1; // TODO
        resource.consumeResource(processRegistry.getOrCreateSimulatedProcess(request), absoluteDemand,
                resourceServiceID, onServedCallback);

    }

}

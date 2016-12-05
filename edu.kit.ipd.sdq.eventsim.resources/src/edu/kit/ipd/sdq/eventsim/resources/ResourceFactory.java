package edu.kit.ipd.sdq.eventsim.resources;

import java.util.concurrent.atomic.AtomicLong;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.HDDProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.scheduler.IPassiveResource;
import de.uka.ipd.sdq.scheduler.ISchedulingFactory;
import de.uka.ipd.sdq.scheduler.SchedulerModel;
import de.uka.ipd.sdq.scheduler.factory.SchedulingFactory;
import de.uka.ipd.sdq.simucomframework.resources.SchedulingStrategy;
import de.uka.ipd.sdq.simucomframework.resources.SimSimpleFairPassiveResource;
import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimActiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimLinkingResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimPassiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimResourceFactory;

@Singleton
public class ResourceFactory {

    private static final String FCFS = "FCFS";

    private static final String PROCESSOR_SHARING = "ProcessorSharing";

    private static final String DELAY = "Delay";

    private static AtomicLong idGenerator = new AtomicLong(0);

    private ISchedulingFactory schedulingFactory;

    @Inject
    private SimResourceFactory resourceFactory;

    private SchedulerModel model;

    @Inject
    public ResourceFactory(SchedulerModel model) {
        this.model = model;
        schedulingFactory = new SchedulingFactory(model);
    }

    /**
     * Creates an active resource in accordance with the given resource specification.
     * 
     * @param model
     *            the simulation model
     * @param specification
     *            the resource specification
     * @return the created resource
     */
    public SimActiveResource createActiveResource(final ProcessingResourceSpecification specification) {
        // TODO reliability stuff
        // double mttf = specification.getMTTF();
        // double mttr = specification.getMTTR();
        final int numberOfReplicas = specification.getNumberOfReplicas();
        final PCMRandomVariable processingRate = specification.getProcessingRate_ProcessingResourceSpecification();
        final String schedulingPolicyId = specification.getSchedulingPolicy().getId();

        IActiveResource resource = null;
        String resourceName;
        switch (schedulingPolicyId) {
        case FCFS:
            resourceName = SchedulingStrategy.FCFS.toString();
            resource = schedulingFactory.createSimFCFSResource(resourceName, getNextResourceId());
            break;
        case DELAY:
            resourceName = SchedulingStrategy.DELAY.toString();
            resource = schedulingFactory.createSimDelayResource(resourceName, getNextResourceId());
            break;
        case PROCESSOR_SHARING:
            resourceName = SchedulingStrategy.PROCESSOR_SHARING.toString();
            resource = schedulingFactory.createSimProcessorSharingResource(resourceName, getNextResourceId(),
                    numberOfReplicas);
            break;
        default:
            // try instantiating resource from extension point, used e.g. by exact schedulers
            resource = schedulingFactory.createResourceFromExtension(specification.getSchedulingPolicy().getId(),
                    getNextResourceId(), numberOfReplicas);
            resourceName = specification.getSchedulingPolicy().getEntityName();
            // TODO do we need to initialize the resource by calling a method as SimuCom does?

            if (resource == null) {
                throw new EventSimException("Unknown scheduling policy: " + schedulingPolicyId.toString());
            }
        }

        SimActiveResource r = null;
        // special case for HDD resources
        if (specification instanceof HDDProcessingResourceSpecification) {
            HDDProcessingResourceSpecification hdd = (HDDProcessingResourceSpecification) specification;
            r = this.resourceFactory.createActiveHDDResource(resource, processingRate.getSpecification(),
                    numberOfReplicas, specification.getSchedulingPolicy(), hdd,
                    hdd.getWriteProcessingRate().getSpecification(), hdd.getReadProcessingRate().getSpecification());
        } else { // normal case (no HDD resource)
            r = this.resourceFactory.createActiveResource(resource, processingRate.getSpecification(), numberOfReplicas,
                    specification.getSchedulingPolicy(), specification);
        }

        return r;
    }

    /**
     * Creates a linking resource in accordance with the given resource specification.
     * 
     * @param model
     *            the simulation model
     * @param specification
     *            the resource specification
     * @return the created resource
     */
    public SimLinkingResource createLinkingResource(final ISimulationModel model,
            final CommunicationLinkResourceSpecification specification) {

        final PCMRandomVariable latency = specification.getLatency_CommunicationLinkResourceSpecification();
        final PCMRandomVariable throughput = specification.getThroughput_CommunicationLinkResourceSpecification();

        String resourceName = SchedulingStrategy.FCFS.toString();
        IActiveResource resource = schedulingFactory.createSimFCFSResource(resourceName, getNextResourceId());

        SimLinkingResource r = resourceFactory.createLinkingResource(resource, latency.getSpecification(),
                throughput.getSpecification(), specification);

        return r;
    }

    /**
     * Creates a passive resource in accordance with the given resource specification.
     * 
     * @param model
     *            the simulation model
     * @param specification
     *            the resource specification
     * @param operatingSystem
     *            the operating system managing the passive resource
     * @param assemblyCtx
     *            the assembly context in which the passive resource is created
     * @return the created resource
     */
    public SimPassiveResource createPassiveResource(final PassiveResource specification,
            final AssemblyContext assemblyCtx) {
        // obtain capacity by evaluating the associated StoEx
        final PCMRandomVariable capacitySpecification = specification.getCapacity_PassiveResource();
        final int capacity = StackContext.evaluateStatic(capacitySpecification.getSpecification(), Integer.class);

        // create the scheduler resource for the operating system
        IPassiveResource schedulerResource = new SimSimpleFairPassiveResource(specification, assemblyCtx,
                (SchedulerModel) model, new Long(capacity)); // TODO get rid of cast

        SimPassiveResource r = resourceFactory.createPassiveResource(schedulerResource, specification);

        return r;
    }

    /**
     * Creates a unique resource ID.
     * 
     * @return the ID
     */
    private static String getNextResourceId() {
        return Long.toString(idGenerator.incrementAndGet());
    }

}

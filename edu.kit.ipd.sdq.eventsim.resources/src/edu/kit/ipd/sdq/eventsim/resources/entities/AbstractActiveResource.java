package edu.kit.ipd.sdq.eventsim.resources.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourcetype.SchedulingPolicy;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.scheduler.ISchedulableProcess;
import de.uka.ipd.sdq.scheduler.sensors.IActiveResourceStateSensor;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.resources.listener.IDemandListener;
import edu.kit.ipd.sdq.eventsim.resources.listener.IStateListener;

public abstract class AbstractActiveResource extends EventSimEntity {

    private static final Logger logger = Logger.getLogger(AbstractActiveResource.class);

    /** the encapsulated scheduler resource */
    protected final IActiveResource schedulerResource;

    private final int numberOfInstances;

    private final Map<Integer, List<IStateListener>> stateListener;
    private final List<IDemandListener> demandListener;
    private final SchedulingPolicy schedulingStrategy;
    private final Set<ISchedulableProcess> registeredProcesses;

    private long[] queueLength;

    public AbstractActiveResource(ISimulationModel simulationModel, String namePrefix,
            IActiveResource schedulerResource, SchedulingPolicy schedulingStrategy, int numberOfInstances) {
        super(simulationModel, namePrefix);

        this.schedulerResource = schedulerResource;
        this.schedulingStrategy = schedulingStrategy;
        this.numberOfInstances = numberOfInstances;

        setupStateListenerAdapter(schedulerResource);

        stateListener = new HashMap<>();
        for (int instance = 0; instance < numberOfInstances; instance++) {
            stateListener.put(instance, new ArrayList<IStateListener>());
        }
        demandListener = new ArrayList<IDemandListener>();
        queueLength = new long[numberOfInstances];
        registeredProcesses = new HashSet<>();
    }

    /**
     * Translates the {@link IActiveResourceStateSensor} listener to the {@link IStateListener}. As
     * a result, all {@link IStateListener} of this resource get notified if the encapsulated
     * scheduler resource fires a {@link IActiveResourceStateSensor} event.
     */
    protected void setupStateListenerAdapter(IActiveResource resource) {
        resource.addObserver(new IActiveResourceStateSensor() {
            @Override
            public void update(long state, int instanceId) {
                queueLength[instanceId] = state;
                fireStateEvent(state, instanceId);
            }

            @Override
            public void demandCompleted(ISchedulableProcess simProcess) {
                // do nothing
            }
        });
    }

    /**
     * Processes the specified demand issued by the given process.
     * 
     * @param process
     *            the process that has requested the demand
     * @param abstractDemand
     *            the demand
     * @param resourceServiceID
     *            the resource service ID
     * @param onServedCallback
     *            called when the requested demand has been served
     */
    public void consumeResource(final SimulatedProcess process, final double abstractDemand,
            final int resourceServiceID, Procedure onServedCallback) {
        if (logger.isDebugEnabled()) {
            logger.debug("Requested resource " + schedulerResource + " with an abstract demand of " + abstractDemand);
        }
        double concreteDemand = calculateConcreteDemand(abstractDemand, resourceServiceID);

        // register process the first time it hits this resource
        if (!registeredProcesses.contains(process)) {
            schedulerResource.registerProcess(process);
            registeredProcesses.add(process);
        }

        process.setOnActivationCallback(onServedCallback);
        schedulerResource.process(process, resourceServiceID, Collections.emptyMap(), concreteDemand);

        // notify demands listeners
        fireDemand(process, concreteDemand, resourceServiceID);
    }

    protected abstract double calculateConcreteDemand(double abstractDemand, int resourceServiceId);

    /**
     * @return the number of instances (e.g., cores in case of a processor) that constitute this
     *         resource.
     */
    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    /**
     * @return the number of jobs in the resource's queue waiting to be processed. TODO: check, if
     *         this value is rather the queue length plus (!) the number of jobs being processed at
     *         the moment.
     */
    public long getQueueLength(int instanceId) {
        return queueLength[instanceId];
    }

    public SchedulingPolicy getSchedulingStrategy() {
        return schedulingStrategy;
    }

    public void addDemandListener(IDemandListener listener) {
        demandListener.add(listener);
    }

    public void addStateListener(final IStateListener listener, int instance) {
        stateListener.get(instance).add(listener);
    }

    /**
     * Notifies the demand listeners that the specified demand has been requested.
     * 
     * @param process
     *            the requesting process
     * @param demand
     *            the requested demand
     * @param resourceServiceID
     *            the resource service id
     */
    protected void fireDemand(ISchedulableProcess process, double demand, int resourceServiceID) {
        for (IDemandListener l : demandListener) {
            l.demand(process, demand, resourceServiceID);
        }
    }

    /**
     * Notifies the state listeners that the state of the specified instance has changed.
     * 
     * @param state
     *            the resource's queue length
     * @param instance
     *            the affected resource instance
     */
    protected void fireStateEvent(long state, int instance) {
        for (IStateListener l : stateListener.get(instance)) {
            l.stateChanged(state, instance);
        }
    }

    /**
     * Called to notify this resource that the simulation run has stopped.
     */
    public void deactivateResource() {
        schedulerResource.stop();
    }

}
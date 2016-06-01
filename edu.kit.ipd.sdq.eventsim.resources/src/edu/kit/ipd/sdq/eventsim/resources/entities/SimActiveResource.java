package edu.kit.ipd.sdq.eventsim.resources.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.omg.CORBA.Request;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.scheduler.ISchedulableProcess;
import de.uka.ipd.sdq.scheduler.sensors.IActiveResourceStateSensor;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.resources.SchedulingPolicy;
import edu.kit.ipd.sdq.eventsim.resources.listener.IDemandListener;
import edu.kit.ipd.sdq.eventsim.resources.listener.IOverallUtilizationListener;
import edu.kit.ipd.sdq.eventsim.resources.listener.IStateListener;

/**
 * An active resource can process demands of {@link Request}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class SimActiveResource extends EventSimEntity {

	private static Logger logger = Logger.getLogger(SimActiveResource.class);

	/** the encapsulated scheduler resource */
	private IActiveResource schedulerResource;
	private String processingRate;
	private int numberOfInstances;
	private Map<Integer, List<IStateListener>> stateListener;
	private List<IDemandListener> demandListener;
	private List<IOverallUtilizationListener> overallUtilizationListener;
	private SchedulingPolicy schedulingStrategy;
	private long[] queueLength;
	private ProcessingResourceSpecification specification;

	/**
	 * Constructs an active resource that wraps the specified resource.
	 * 
	 * @param model
	 *            the simulation model
	 * @param resource
	 *            the wrapped scheduler resource
	 * @param processingRate
	 * @param numberOfInstances
	 * @param specification
	 */
	@Inject
    public SimActiveResource(ISimulationModel model, @Assisted IActiveResource resource,
            @Assisted String processingRate, @Assisted int numberOfInstances,
            @Assisted SchedulingPolicy schedulingStrategy, @Assisted ProcessingResourceSpecification specification) {
		super(model, "SimActiveResource");
		this.schedulerResource = resource;
		this.processingRate = processingRate;
		this.numberOfInstances = numberOfInstances;
		this.schedulingStrategy = schedulingStrategy;
		this.specification = specification;

		this.setupStateListenerAdapter(this.schedulerResource);
		stateListener = new HashMap<Integer, List<IStateListener>>();
		for (int instance = 0; instance < numberOfInstances; instance++) {
			stateListener.put(instance, new ArrayList<IStateListener>());
		}
		overallUtilizationListener = new ArrayList<IOverallUtilizationListener>();
		demandListener = new ArrayList<IDemandListener>();
		queueLength = new long[numberOfInstances];
	}

	/**
	 * Translates the {@link IActiveResourceStateSensor} listener to the {@link IStateListener}. As a result, all
	 * {@link IStateListener} of this resource get notified if the encapsulated scheduler resource fires a
	 * {@link IActiveResourceStateSensor} event.
	 */
	private void setupStateListenerAdapter(IActiveResource resource) {
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
	 */
	public void consumeResource(ISchedulableProcess process, double abstractDemand) {
		if (logger.isDebugEnabled()) {
			logger.debug("Requested resource " + schedulerResource + " with an abstract demand of " + abstractDemand);
		}
		double concreteDemand = calculateConcreteDemand(abstractDemand);

		// TODO What resource service ID has to passed here?
		schedulerResource.process(process, 1, Collections.<String, Serializable> emptyMap(), concreteDemand);

		// notify demands listeners
		fireDemand(process, concreteDemand);
	}

	protected double calculateConcreteDemand(double abstractDemand) {
		return abstractDemand / Context.evaluateStatic(processingRate, Double.class);
	}

	/**
	 * Returns the resource ID.
	 * 
	 * @return the resource's ID
	 * 
	 * @see IActiveResource#getId()
	 */
	public String getId() {
		return specification.getId();
	}

	/**
	 * Returns the name of the resource.
	 * 
	 * @return the resource's name
	 * 
	 * @see IActiveResource#getName()
	 */
	public String getName() {
		// obtain entity name (HDD, CPU, ...) from specification
		String resourceContainerName = specification.getResourceContainer_ProcessingResourceSpecification()
				.getEntityName();
		String resourceTypeName = specification.getActiveResourceType_ActiveResourceSpecification().getEntityName();
		return resourceContainerName + " [" + resourceTypeName + "]";
	}

	/**
	 * @return the number of instances (e.g., cores in case of a processor) that constitute this resource.
	 */
	public int getNumberOfInstances() {
		return numberOfInstances;
	}

	/**
	 * @return the number of jobs in the resource's queue waiting to be processed. TODO: check, if this value is rather
	 *         the queue length plus (!) the number of jobs being processed at the moment.
	 */
	public long getQueueLength(int instanceId) {
		return queueLength[instanceId];
	}

	public SchedulingPolicy getSchedulingStrategy() {
		return schedulingStrategy;
	}

	public ProcessingResourceSpecification getSpecification() {
		return specification;
	}

	public void addDemandListener(IDemandListener listener) {
		demandListener.add(listener);
	}

	public void addOverallUtilizationListener(IOverallUtilizationListener listener) {
		overallUtilizationListener.add(listener);
	}

	public void addStateListener(final IStateListener listener, int instance) {
		stateListener.get(instance).add(listener);
	}

	/**
	 * Notifies the demand listeners that the specified demand has been requested.
	 * @param process 
	 * 
	 * @param demand
	 *            the requested demand
	 */
	protected void fireDemand(ISchedulableProcess process, double demand) {
		for (IDemandListener l : demandListener) {
			l.demand(process, demand);
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

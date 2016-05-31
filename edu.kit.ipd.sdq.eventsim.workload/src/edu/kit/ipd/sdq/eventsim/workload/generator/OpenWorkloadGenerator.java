package edu.kit.ipd.sdq.eventsim.workload.generator;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.OpenWorkload;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.events.WorkloadUserFinishedEvent;
import edu.kit.ipd.sdq.eventsim.entities.IEntityListener;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.events.BeginUsageTraversalEvent;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.UsageBehaviourInterpreter;

/**
 * An open workload generates a new {@link User} as soon as a specified time duration has passed
 * since the previous user has been created. This time duration between two subsequent user arrivals
 * is called the interarrival time.
 * 
 * @author Philipp Merkle
 * 
 */
public class OpenWorkloadGenerator implements IWorkloadGenerator {

    private final OpenWorkload workload;
    private final PCMRandomVariable interarrivalTime;

    private ISimulationModel model;
    private ISimulationMiddleware middleware;
    private UserFactory userFactory; 
    private Provider<UsageBehaviourInterpreter> interpreterProvider; 
    
    /**
     * Constructs an open workload in accordance with the specified workload description.
     * 
     * @param model
     *            the model
     * @param workload
     *            the workload description
     */
    @Inject
    public OpenWorkloadGenerator(ISimulationModel model, ISimulationMiddleware middleware,
            Provider<UsageBehaviourInterpreter> interpreterProvider, UserFactory userFactory,
            @Assisted final OpenWorkload workload) {
        this.model = model;
        this.middleware = middleware;
        this.interpreterProvider = interpreterProvider;
        this.userFactory = userFactory;
        this.workload = workload;
        
        this.interarrivalTime = workload.getInterArrivalTime_OpenWorkload();
    }

    /**
     * {@inheritDoc}
     */
    public void processWorkload() {
        // spawn initial user
        this.spawnUser(0);
    }

    /**
     * Creates a new user and schedule the next user to enter the system after the interarrival time
     * has passed.
     */
    private void spawnUser(double waitingTime) {
        // create the user
        final UsageScenario scenario = this.workload.getUsageScenario_Workload();
        User user = userFactory.create(scenario);

        // when the user entered the system, we wait until the interarrival time has passed and then
        // schedule a new one
        user.addEntityListener(new IEntityListener() {

            @Override
            public void enteredSystem() {
                final double waitingTime = StackContext.evaluateStatic(OpenWorkloadGenerator.this.interarrivalTime.getSpecification(), Double.class);
                OpenWorkloadGenerator.this.spawnUser(waitingTime);
            }

            @Override
            public void leftSystem() {
            	// trigger event that the user finished his work
            	middleware.triggerEvent(new WorkloadUserFinishedEvent(user));
            }

        });
        
        new BeginUsageTraversalEvent(model, scenario, middleware, interpreterProvider.get()).schedule(user, waitingTime);
    }

}

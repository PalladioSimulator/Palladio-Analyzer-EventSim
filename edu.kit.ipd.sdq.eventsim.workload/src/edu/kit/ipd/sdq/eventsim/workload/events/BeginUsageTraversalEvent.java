package edu.kit.ipd.sdq.eventsim.workload.events;

import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.events.WorkloadUserSpawn;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.UsageBehaviourInterpreter;

/**
 * Schedule this event to begin the traversal of a {@link UsageScenario}.
 * <p>
 * The {@link User} that is supposed to traverse the scenario, is passed to the {@code schedule()}
 * method.
 * 
 * @author Philipp Merkle
 * 
 */
public class BeginUsageTraversalEvent extends AbstractSimEventDelegator<User> {

    private final UsageScenario scenario;

    private ISimulationMiddleware middleware;

    private UsageBehaviourInterpreter interpreter;
    
    /**
     * Use this constructor to begin the traversal of the specified {@link UsageScenario}.
     * 
     * @param model
     *            the model
     * @param scenario
     *            the usage scenario that is to be traversed
     */
    public BeginUsageTraversalEvent(final ISimulationModel model, final UsageScenario scenario, ISimulationMiddleware middleware, UsageBehaviourInterpreter interpreter) {
        super(model, "BeginUsageTraversalEvent");
        this.scenario = scenario;
        this.middleware = middleware;
        this.interpreter = interpreter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventRoutine(final User who) {
    	// trigger event that a user spawned
    	middleware.triggerEvent(new WorkloadUserSpawn(who));

        ScenarioBehaviour behaviour = this.scenario.getScenarioBehaviour_UsageScenario();
        interpreter.beginTraversal(who, behaviour);
    }

}

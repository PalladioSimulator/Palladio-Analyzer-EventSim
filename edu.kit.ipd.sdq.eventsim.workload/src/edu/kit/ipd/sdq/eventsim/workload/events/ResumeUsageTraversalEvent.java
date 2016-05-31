package edu.kit.ipd.sdq.eventsim.workload.events;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.UsageBehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

/**
 * Schedule this event to resume the traversal of a {@link UsageScenario}.
 * <p>
 * The {@link User} that is supposed to traverse the scenario, is passed to the {@code schedule()}
 * method.
 * 
 * @author Philipp Merkle
 * 
 */
public class ResumeUsageTraversalEvent extends AbstractSimEventDelegator<User> {

    private final UserState state;
		
	private UsageBehaviourInterpreter interpreter;
	
    /**
     * Use this constructor to resume the traversal of a {@link UsageScenario}. All information
     * required to resume the traversal are contained in the specified traversal {@code state}.
     * 
     * @param model
     *            the model
     * @param state
     *            the traversal state
     */
    public ResumeUsageTraversalEvent(final ISimulationModel model, final UserState state, UsageBehaviourInterpreter interpreter) {
        super(model, "ResumeUsageTraversalEvent");
        this.state = state;
        this.interpreter = interpreter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventRoutine(final User who) {
        interpreter.resumeTraversal(who, this.state);
    }

}

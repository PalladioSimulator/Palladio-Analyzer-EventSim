package edu.kit.ipd.sdq.eventsim.workload.interpreter;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.useraction.FindActionInUsageBehaviour;
import edu.kit.ipd.sdq.eventsim.interpreter.BehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

/**
 * An interpreter for {@link UsageScenario}s.
 * 
 * @author Philipp Merkle
 * 
 * @see BehaviourInterpreter
 */
@Singleton
public class UsageBehaviourInterpreter extends BehaviourInterpreter<AbstractUserAction, User, UserState> {

    @Inject
    private WorkloadModelDiagnostics diagnostics;

    @Inject
    private PCMModelCommandExecutor executor;

    @Inject
    private TraversalListenerRegistry<AbstractUserAction, User, UserState> listenerRegistry;

    /**
     * {@inheritDoc}
     */
    public void beginTraversal(final User user, final ScenarioBehaviour behaviour) {
        user.notifyEnteredSystem();

        // initialise traversal state and StoEx context
        UserState state = new UserState();
        state.pushStackFrame();
        state.getStoExContext().getStack().createAndPushNewStackFrame();
        user.setUserState(state);

        // find start action
        final Start start = executor.execute(new FindActionInUsageBehaviour<Start>(behaviour, Start.class));

        // begin traversal
        state.setCurrentPosition(start);
        this.traverse(user, start, state);
    }

    /**
     * {@inheritDoc}
     */
    public void resumeTraversal(final User user, final UserState state) {
        // find next action and resume traversal
        super.traverse(user, state.getCurrentPosition(), state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAfterListener(final AbstractUserAction action, final User user, UserState state) {
        listenerRegistry.getTraversalListenerList().forEach(l -> l.after(action, user, state));

        if (listenerRegistry.getTraversalListenerMap().containsKey(action)) {
            listenerRegistry.getTraversalListenerMap().get(action).forEach(l -> l.after(action, user, state));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyBeforeListener(final AbstractUserAction action, final User user, UserState state) {
        listenerRegistry.getTraversalListenerList().forEach(l -> l.before(action, user, state));

        if (listenerRegistry.getTraversalListenerMap().containsKey(action)) {
            listenerRegistry.getTraversalListenerMap().get(action).forEach(l -> l.before(action, user, state));
        }
    }

    public WorkloadModelDiagnostics getDiagnostics() {
        return diagnostics;
    }

}

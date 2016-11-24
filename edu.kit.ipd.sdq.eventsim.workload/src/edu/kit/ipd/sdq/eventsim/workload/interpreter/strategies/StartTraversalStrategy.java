package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Start;

import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

/**
 * This traversal strategy is responsible for {@link Start} actions.
 * 
 * @author Philipp Merkle
 * 
 */
public class StartTraversalStrategy extends DecoratingTraversalStrategy<AbstractUserAction, Start, User, UserState> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractUserAction, UserState> traverse(final Start start, final User user,
            final UserState state) {
        traverseDecorated(start, user, state);
        return new TraverseNextAction<>(start.getSuccessor());
    }

}

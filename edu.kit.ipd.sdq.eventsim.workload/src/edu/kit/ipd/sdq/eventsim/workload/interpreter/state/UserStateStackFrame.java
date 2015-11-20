package edu.kit.ipd.sdq.eventsim.workload.interpreter.state;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.interpreter.state.AbstractStateStackFrame;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;

/**
 * A stack frame which holds the state of a {@link User} entity.
 * 
 * @author Philipp Merkle
 * 
 * @see UserState
 */
public class UserStateStackFrame extends AbstractStateStackFrame<AbstractUserAction> implements IUserState {

    // currently, there is no additional state information for User entities

    @Override
    public Object clone() throws CloneNotSupportedException {
        // currently, there is no need for cloning stack frames of users
        throw new CloneNotSupportedException();
    }

}

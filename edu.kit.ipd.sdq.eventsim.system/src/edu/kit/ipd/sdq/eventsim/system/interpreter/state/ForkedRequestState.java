package edu.kit.ipd.sdq.eventsim.system.interpreter.state;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;

/**
 * 
 * @author Philipp Merkle
 *
 */
public class ForkedRequestState extends RequestState {

    private IRequestState parentRequestState;

    private ForkedRequestState(StackContext stoExContext) {
        super(stoExContext);
    }

    public ForkedRequestState(RequestState requestState, StackContext stoExContext) {
        this(stoExContext);
        this.parentRequestState = requestState;
    }

    public IRequestState getParentRequestState() {
        return this.parentRequestState;
    }

    public boolean isForkedRequestState() {
        return true;
    }
    
}

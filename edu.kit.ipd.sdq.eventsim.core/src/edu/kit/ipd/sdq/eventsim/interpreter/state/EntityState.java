package edu.kit.ipd.sdq.eventsim.interpreter.state;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;

/**
 * This abstract class specifies methods for a stack-like data structure that captures the
 * simulation progress (state) of an {@link EventSimEntity}. The organisation as a stack takes into
 * account the potentially hierarchical nesting of actions such as Loops and Branches.
 * <p>
 * For each level of hierarchy, there is a stack frame, which holds the state information for the
 * respective level of hierarchy. Only state information contained in the topmost frame are
 * accessible.
 * 
 * @author Philipp Merkle
 * 
 * @param <A>
 *            the least common parent type of all actions that are to be traversed
 */
public class EntityState<A extends Entity> implements IEntityState<A> {

    private static final Logger logger = Logger.getLogger(EntityState.class);

    private final Stack<StateStackFrame<A>> stack;

    private final StackContext stoExContext;

    /**
     * Copy constructor.
     * 
     * @param state
     *            the state to be copied
     */
    public EntityState(EntityState<A> state) {
        this.stack = new Stack<>(); // TODO better copy stack?
        this.stoExContext = new StackContext();
        this.stoExContext.getStack().pushStackFrame(state.getStoExContext().getStack().currentStackFrame().copyFrame());

        StackContext stoExContextCopy = new StackContext();
        stoExContextCopy.getStack().pushStackFrame(this.stoExContext.getStack().currentStackFrame().copyFrame());
    }

    public EntityState(final StackContext stoExContext) {
        this.stack = new Stack<>();
        this.stoExContext = stoExContext;
    }

    /**
     * Pushes an emtpy stack frame onto the stack.
     */
    public void pushStackFrame() {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering scope");
        }
        final StateStackFrame<A> f = new StateStackFrame<>();
        this.stack.push(f);
    }

    /**
     * Removes the topmost frame from the stack.
     */
    public void popStackFrame() {
        assert !this.stack.isEmpty() : "Tried to leave scope but there is no outer scope";
        if (logger.isDebugEnabled()) {
            logger.debug("Leaving scope");
        }
        this.stack.pop();
    }

    /**
     * Returns whether the stack is empty.
     * 
     * @return true, if the stack is empty; false else
     */
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    /**
     * @return the number of stack frames contained in this state's stack
     */
    public int size() {
        return stack.size();
    }

    @Override
    public void setOnFinishCallback(Procedure callback) {
        stack.peek().setOnFinishCallback(callback);
    }

    @Override
    public Procedure getOnFinishCallback() {
        return stack.peek().getOnFinishCallback();
    }

    @Override
    public A getCurrentPosition() {
        return this.stack.peek().getCurrentPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentPosition(A position) {
        this.stack.peek().setCurrentPosition(position);
    }

    /**
     * Returns the context that is used to evaluate stochastic expressions (StoEx). The context
     * comprises a stack that contains the local variables of service calls. While traversing a
     * {@link ResourceDemandingSEFF}, the stack content changes according to the traversal progress.
     * 
     * @return the evaluation context for stochastic expressions
     */
    public StackContext getStoExContext() {
        return this.stoExContext;
    }

    @Override
    public void addProperty(String name, Object property) {
        this.stack.peek().addProperty(name, property);
    }

    @Override
    public <T> T getProperty(String name, Class<T> type) {
        return this.stack.peek().getProperty(name, type);
    }

}

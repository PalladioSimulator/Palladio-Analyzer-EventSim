package edu.kit.ipd.sdq.eventsim.interpreter;

import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.interpreter.state.AbstractInterpreterState;

public abstract class DecoratingTraversalStrategy<A extends Entity, T extends A, E extends EventSimEntity, S extends AbstractInterpreterState<A>>
        implements ITraversalStrategy<A, T, E, S> {

    protected ITraversalStrategy<A, T, E, S> decorated;

    public void decorate(ITraversalStrategy<A, T, E, S> decorated) {
        this.decorated = decorated;
    }

    protected ITraversalInstruction<A, S> traverseDecorated(T action, E entity, S state) {
        if (decorated != null) {
            return decorated.traverse(action, entity, state);
        }
        return null;
    }

}

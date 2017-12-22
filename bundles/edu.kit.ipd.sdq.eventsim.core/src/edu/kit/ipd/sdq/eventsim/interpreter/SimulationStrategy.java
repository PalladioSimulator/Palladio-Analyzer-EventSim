package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;

public interface SimulationStrategy<A extends Entity, E extends EventSimEntity> {

    /**
     * Simulates a particular behavioural aspect of an {@code entity}, expressed by the given
     * {@code action}.
     * <p>
     * For example, let's assume the given action describes a call to another component, and let the
     * entity be a request (or process). The corresponding strategy would then simulate the
     * request's effects of calling the external component service.
     * <p>
     * Once desired effects have been simulated, this simulation strategy must provide a traversal
     * instruction to its caller by invoking the passed callback.
     * 
     * @param action
     *            the action to be simulated as part of the entity's behaviour
     * @param entity
     *            the entity whose behaviour is to be simulated by this strategy
     * @param onFinishCallback
     *            the callback that must be invoked once this strategy finishes simulation; a
     *            traversal instruction is passed as argument to the callback
     */
    public void simulate(A action, E entity, Consumer<TraversalInstruction> onFinishCallback);

}

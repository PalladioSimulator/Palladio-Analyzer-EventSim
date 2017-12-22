package edu.kit.ipd.sdq.eventsim.interpreter.listener;

import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;

/**
 * A traversal listener observes subclasses of {@link BehaviourInterpreter} for their traversal
 * progress. Whenever an action is about to be traversed or has been traversed completely, the
 * BehaviourTraversal notifies its observers by calling either the {@code before} or the {@code
 * after} method on the listeners.
 * 
 * @author Philipp Merkle
 * 
 * @param <A>
 *            the least common parent type of all actions that are to be traversed
 * @param <E>
 *            the type of the entity whose behaviour is simulated by the traversal
 */
public interface ITraversalListener<A extends Entity, E extends EventSimEntity> {

    /**
     * Called by the {@link BehaviourInterpreter} when the specified action is about to be traversed
     * by the given entity.
     * 
     * @param action
     *            the action that is traversed soon
     * @param entity
     *            the entity that traverses the action
     */
    public void before(A action, E entity);

    /**
     * Called by the {@link BehaviourInterpreter} when the specified action has been traversed
     * completely by the given entity.
     * 
     * @param action
     *            the action that has been traversed
     * @param entity
     *            the entity that has traversed the action
     */
    public void after(A action, E entity);

}

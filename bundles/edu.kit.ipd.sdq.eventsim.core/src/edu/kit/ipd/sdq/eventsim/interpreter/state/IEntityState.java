package edu.kit.ipd.sdq.eventsim.interpreter.state;

import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.api.Procedure;

/**
 * This interface specifies which state information can be set and returned to describe the progress
 * of an {@link BehaviourInterpreter}.
 * 
 * @author Philipp Merkle
 * 
 */
public interface IEntityState<A extends Entity> {

    /**
     * Returns the current position of the traversal.
     * 
     * @return the action that is being traversed currently
     */
    public A getCurrentPosition();

    /**
     * Sets the current position of the traversal.
     * 
     * @param position
     *            the action that is being traversed currently
     */
    public void setCurrentPosition(final A position);

    public void setOnFinishCallback(Procedure onFinishCallback);

    public Procedure getOnFinishCallback();

    void addProperty(String name, Object property);

    <T> T getProperty(String name, Class<T> type);

}

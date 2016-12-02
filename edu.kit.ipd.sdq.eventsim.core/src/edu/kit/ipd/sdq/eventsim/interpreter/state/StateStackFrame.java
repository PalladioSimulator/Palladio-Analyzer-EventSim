package edu.kit.ipd.sdq.eventsim.interpreter.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.api.Procedure;

/**
 * A stack frame which holds information of the traversal progress.
 * 
 * @author Philipp Merkle
 * 
 * @param <A>
 *            the least common parent type of all actions that are to be traversed
 * @see EntityState
 */
public class StateStackFrame<A extends Entity> implements IInterpreterState<A> {

    private A currentPosition;

    private Procedure onFinishCallback;

    private Map<String, Object> properties;

    @Override
    public A getCurrentPosition() {
        return this.currentPosition;
    }

    @Override
    public void setCurrentPosition(final A position) {
        this.currentPosition = position;
    }

    @Override
    public void setOnFinishCallback(Procedure onFinishCallback) {
        this.onFinishCallback = onFinishCallback;
    }

    @Override
    public Procedure getOnFinishCallback() {
        return onFinishCallback;
    }

    @Override
    public void addProperty(String name, Object property) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(Objects.requireNonNull(name), Objects.requireNonNull(property));
    }

    /**
     * 
     * @param name
     *            the properties name
     * @return the property, or null if there is no property for the given name
     */
    @Override
    public <T> T getProperty(String name, Class<T> type) {
        if (properties == null) {
            return null;
        }
        return (T) properties.get(name);
    }

}

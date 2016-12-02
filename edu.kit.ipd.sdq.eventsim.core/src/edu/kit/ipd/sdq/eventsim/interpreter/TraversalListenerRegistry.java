package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.seff.AbstractAction;

import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.interpreter.listener.ITraversalListener;

@Singleton
public class TraversalListenerRegistry<A extends Entity, E extends EventSimEntity> {

    private final Map<A, List<ITraversalListener<A, E>>> traversalListenerMap = new HashMap<>();
    private final List<ITraversalListener<A, E>> traversalListenerList = new ArrayList<>();

    /**
     * Adds a traversal listener that is notified whenever the specified action is about to be
     * traversed or has been traversed completely.
     * 
     * @param action
     *            the action that is to be observed
     * @param listener
     *            the listener that is to be registered
     */
    public void addTraversalListener(final A action, final ITraversalListener<A, E> listener) {
        if (!traversalListenerMap.containsKey(action)) {
            traversalListenerMap.put(action, new ArrayList<>());
        }
        traversalListenerMap.get(action).add(listener);
    }

    /**
     * Adds a traversal listener that is notified whenever an arbitrary action is about to be
     * traversed or has been traversed completely.
     * 
     * @param listener
     *            the listener that is to be registered
     */
    public void addTraversalListener(final ITraversalListener<A, E> listener) {
        traversalListenerList.add(listener);
    }

    /**
     * Removes the specified traversal listener that has been registered to listen for traversal
     * events associated with the specified action.
     * 
     * @param action
     *            the action that is observed by the specified listener
     * @param listener
     *            the listener that is to be unregistered
     */
    public void removeTraversalListener(final AbstractAction action, final ITraversalListener<A, E> listener) {
        traversalListenerMap.get(action).remove(listener);
    }

    public List<ITraversalListener<A, E>> getTraversalListenerList() {
        return Collections.unmodifiableList(traversalListenerList);
    }

    public Map<A, List<ITraversalListener<A, E>>> getTraversalListenerMap() {
        return Collections.unmodifiableMap(traversalListenerMap);
    }

    /**
     * Removes all {@link ITraversalListener}s.
     */
    public void removeTraversalListeners() {
        traversalListenerList.clear();
        traversalListenerMap.clear();
    }

    public void notifyAfterListener(final A action, final E request) {
        for (final ITraversalListener<A, E> l : getTraversalListenerList()) {
            l.after(action, request);
        }
        final List<ITraversalListener<A, E>> listeners = getTraversalListenerMap().get(action);
        if (listeners != null) {
            for (final ITraversalListener<A, E> l : listeners) {
                l.after(action, request);
            }
        }
    }

    public void notifyBeforeListener(final A action, final E request) {
        for (final ITraversalListener<A, E> l : getTraversalListenerList()) {
            l.before(action, request);
        }
        final List<ITraversalListener<A, E>> listeners = getTraversalListenerMap().get(action);
        if (listeners != null) {
            for (final ITraversalListener<A, E> l : listeners) {
                l.before(action, request);
            }
        }
    }

}
package edu.kit.ipd.sdq.eventsim.interpreter.state;

import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;

/**
 * The internal state of an {@link ITraversalStrategy}. Traversal strategies that need to store
 * their state on the {@link TraversalStateStack} implement this interface to be able to store their
 * state in an {@link AbstractStateStackFrame}.
 * 
 * @author Philipp Merkle
 * 
 */
public interface ITraversalStrategyState extends Cloneable {

    public void setProperty(Object key, Object value);

    public <T> T getProperty(Object key, T defaultValue);
    
    public boolean hasProperty(Object key);

    Object clone() throws CloneNotSupportedException;

}

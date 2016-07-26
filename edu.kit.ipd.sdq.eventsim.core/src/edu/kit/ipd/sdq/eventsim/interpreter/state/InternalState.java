package edu.kit.ipd.sdq.eventsim.interpreter.state;

import java.util.HashMap;
import java.util.Map;

public class InternalState implements ITraversalStrategyState {

    private Map<Object, Object> properties;

    @Override
    public void setProperty(Object key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

    @Override
    public <T> T getProperty(Object key, T defaultValue) {
        if (properties == null || !properties.containsKey(key)) {
            return defaultValue;
        }
        return (T) properties.get(key);
    }

    @Override
    public boolean hasProperty(Object key) {
        if (properties == null) {
            return false;
        }
        return properties.containsKey(key);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        InternalState copy = new InternalState();
        copy.properties.putAll(properties);
        return copy;
    }

}

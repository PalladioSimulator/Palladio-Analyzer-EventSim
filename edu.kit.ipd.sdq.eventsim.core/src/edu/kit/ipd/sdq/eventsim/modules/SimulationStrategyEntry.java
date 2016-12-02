package edu.kit.ipd.sdq.eventsim.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class SimulationStrategyEntry implements Comparable<SimulationStrategyEntry> {

    private final String name;

    private final String actionType;

    private final Object strategy;

    private final int priority;

    private SimulationStrategyEntry(String name, String actionType, int priority, Object strategy) {
        this.name = name;
        this.actionType = actionType;
        this.priority = priority;
        this.strategy = strategy;
    }

    public String getName() {
        return name;
    }

    public String getActionType() {
        return actionType;
    }

    public Object getStrategy() {
        return strategy;
    }

    public int getPriority() {
        return priority;
    }

    public static SimulationStrategyEntry createFrom(IConfigurationElement config, int priority) {
        String name = config.getAttribute("name");
        String actionType = config.getAttribute("action");
        Object strategy;
        try {
            strategy = config.createExecutableExtension("strategy");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return new SimulationStrategyEntry(name, actionType, priority, strategy);
    }

    @Override
    public int compareTo(SimulationStrategyEntry other) {
        return Integer.compare(priority, other.getPriority());
    }

}

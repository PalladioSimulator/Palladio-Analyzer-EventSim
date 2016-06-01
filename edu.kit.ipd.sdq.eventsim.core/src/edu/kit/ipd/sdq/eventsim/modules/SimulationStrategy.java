package edu.kit.ipd.sdq.eventsim.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class SimulationStrategy {

    private String name;

    private String actionType;

    private Object strategy;

    private SimulationStrategy() {
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

    public static SimulationStrategy createFrom(IConfigurationElement config) {
        SimulationStrategy strategy = new SimulationStrategy();

        strategy.name = config.getAttribute("name");
        strategy.actionType = config.getAttribute("action");
        try {
            strategy.strategy = config.createExecutableExtension("strategy");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        return strategy;
    }

}

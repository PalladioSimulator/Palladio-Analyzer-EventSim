package edu.kit.ipd.sdq.eventsim.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.PlatformUI;

import com.google.inject.Module;

public class SimulationModule implements Comparable<SimulationModule> {

    private static final int PRIORITY_DEFAULT = 100;

    private String name;

    private String id;

    private Module guiceModule;

    private Class<?> entryPoint;

    private int priority;

    private boolean enabled;

    private ILaunchContribution launchContribution;

    private List<SimulationStrategyEntry> simulationStrategies;

    private SimulationModule() {
        simulationStrategies = new ArrayList<>();
    }

    private void addSimulationStrategy(SimulationStrategyEntry strategy) {
        this.simulationStrategies.add(strategy);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Module getGuiceModule() {
        return guiceModule;
    }

    public Class<?> getEntryPoint() {
        return entryPoint;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ILaunchContribution getLaunchContribution() {
        return launchContribution;
    }

    public List<SimulationStrategyEntry> getSimulationStrategies() {
        return Collections.unmodifiableList(simulationStrategies);
    }

    public static SimulationModule createFrom(IConfigurationElement config) {
        SimulationModule module = new SimulationModule();

        String name = config.getAttribute("name");
        String id = config.getAttribute("id");

        // guice module
        Object guiceModule = null;
        try {
            if (config.getAttribute("guice_module") != null) {
                guiceModule = config.createExecutableExtension("guice_module");
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        // entry point
        String entryPointClassName = null;
        Class<?> entryPoint = null;
        if (config.getAttribute("entry_point") != null) {
            entryPointClassName = config.getAttribute("entry_point");
            try {
                entryPoint = Class.forName(entryPointClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        String priority = config.getAttribute("priority");
        Object launchContribution = null;
        try {
            if (PlatformUI.isWorkbenchRunning()) {
                if (config.getAttribute("launch_contribution") != null) {
                    launchContribution = config.createExecutableExtension("launch_contribution");
                } else {
                    launchContribution = new DefaultLaunchContribution();
                }
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        module.name = name;
        module.id = id;
        module.guiceModule = guiceModule != null ? (Module) guiceModule : null;
        module.entryPoint = entryPoint;
        module.priority = priority != null ? Integer.parseInt(priority) : PRIORITY_DEFAULT;
        module.launchContribution = launchContribution != null ? (ILaunchContribution) launchContribution : null;

        for (IConfigurationElement e : config.getChildren("simulation_strategy")) {
            SimulationStrategyEntry s = SimulationStrategyEntry.createFrom(e, module.priority);
            module.addSimulationStrategy(s);
        }

        return module;
    }

    @Override
    public int compareTo(SimulationModule o) {
        return Integer.valueOf(priority).compareTo(Integer.valueOf(o.priority));
    }

}

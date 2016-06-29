package edu.kit.ipd.sdq.eventsim.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.ui.ILaunchConfigurationTab2;

import com.google.inject.Module;

public class SimulationModule implements Comparable<SimulationModule> {

    private static final int PRIORITY_DEFAULT = 100;

    private String name;

    private String id;

    private Module guiceModule;

    private int priority;
    
    private boolean enabled;

    private ILaunchConfigurationTab2 launchContribution;

    private List<SimulationStrategy> simulationStrategies;

    private SimulationModule() {
        simulationStrategies = new ArrayList<>();
    }

    private void addSimulationStrategy(SimulationStrategy strategy) {
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

    public int getPriority() {
        return priority;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ILaunchConfigurationTab2 getLaunchContribution() {
        return launchContribution;
    }

    public List<SimulationStrategy> getSimulationStrategies() {
        return Collections.unmodifiableList(simulationStrategies);
    }

    public static SimulationModule createFrom(IConfigurationElement config) {
        SimulationModule module = new SimulationModule();

        String name = config.getAttribute("name");
        String id = config.getAttribute("id");
        Object guiceModule;
        try {
            guiceModule = config.createExecutableExtension("guice_module");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        String priority = config.getAttribute("priority");
        Object launchContribution = null;
        try {
            if (config.getAttribute("launch_contribution") != null) {
                launchContribution = config.createExecutableExtension("launch_contribution");
            } else {
                launchContribution = new DefaultLaunchContribution();
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        module.name = name;
        module.id = id;
        module.guiceModule = guiceModule != null ? (Module) guiceModule : null;
        module.priority = priority != null ? Integer.parseInt(priority) : PRIORITY_DEFAULT;
        module.launchContribution = launchContribution != null ? (ILaunchConfigurationTab2) launchContribution : null;

        for (IConfigurationElement e : config.getChildren("simulation_strategy")) {
            SimulationStrategy s = SimulationStrategy.createFrom(e);
            module.addSimulationStrategy(s);
        }

        return module;
    }

    @Override
    public int compareTo(SimulationModule o) {
        return Integer.valueOf(priority).compareTo(Integer.valueOf(o.priority));
    }

}

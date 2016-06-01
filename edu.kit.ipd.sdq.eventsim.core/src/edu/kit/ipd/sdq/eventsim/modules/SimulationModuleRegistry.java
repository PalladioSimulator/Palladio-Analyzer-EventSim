package edu.kit.ipd.sdq.eventsim.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

public class SimulationModuleRegistry {

    private List<SimulationModule> modules;

    private SimulationModuleRegistry() {
        modules = new ArrayList<>();
    }

    private void addModule(SimulationModule module) {
        this.modules.add(module);
    }

    public List<SimulationModule> getModules() {
        // sort according to priority, starting with lowest priority
        modules.sort(new Comparator<SimulationModule>() {
            @Override
            public int compare(SimulationModule o1, SimulationModule o2) {
                return o1.compareTo(o2);
            }
        });
        return Collections.unmodifiableList(modules);
    }

    public static SimulationModuleRegistry createFrom(IExtensionRegistry extensionRegistry) {
        SimulationModuleRegistry registry = new SimulationModuleRegistry();

        IConfigurationElement[] configElements = extensionRegistry
                .getConfigurationElementsFor("edu.kit.ipd.sdq.eventsim.module");
        for (IConfigurationElement config : configElements) {
            SimulationModule module = SimulationModule.createFrom(config);
            registry.addModule(module);
        }

        return registry;
    }

}

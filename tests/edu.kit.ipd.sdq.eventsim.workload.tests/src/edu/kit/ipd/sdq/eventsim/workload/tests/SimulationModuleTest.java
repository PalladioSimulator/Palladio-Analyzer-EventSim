package edu.kit.ipd.sdq.eventsim.workload.tests;

import org.eclipse.core.runtime.Platform;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.ILinkingResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddlewareModule;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModule;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModuleRegistry;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystemModule;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModule;

public class SimulationModuleTest extends AbstractModule {

    private ISimulationConfiguration config;

    public SimulationModuleTest(ISimulationConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        install(new SimulationMiddlewareModule(config));
        install(new EventSimWorkloadModule());
        install(new EventSimSystemModule());

        bind(IActiveResource.class).toInstance(Mockito.mock(IActiveResource.class));
        bind(IPassiveResource.class).toInstance(Mockito.mock(IPassiveResource.class));
        bind(ILinkingResource.class).toInstance(Mockito.mock(ILinkingResource.class));

        MeasurementStorage measurementStorage = Mockito.mock(MeasurementStorage.class);
        bind(MeasurementStorage.class).toInstance(measurementStorage);

        SimulationModuleRegistry moduleRegistry = instantiateSimulationModuleRegistry();
        bind(SimulationModuleRegistry.class).toInstance(moduleRegistry);

        bind(ISimulationConfiguration.class).toInstance(config);
        bind(PCMModel.class).toInstance(config.getPCMModel());
        bind(InstrumentationDescription.class).toInstance(Mockito.mock(InstrumentationDescription.class));
    }

    private SimulationModuleRegistry instantiateSimulationModuleRegistry() {
        SimulationModuleRegistry moduleRegistry = SimulationModuleRegistry.createFrom(Platform.getExtensionRegistry());
        // TODO improve method to select enabled simulation modules 
        for (SimulationModule m : moduleRegistry.getModules()) {
            if (m.getId().startsWith("edu.kit.ipd.sdq.eventsim.") && !m.getId().contains("example")) {
                m.setEnabled(true);
            } else {
                m.setEnabled(false);
            }
        }
        return moduleRegistry;
    }

}

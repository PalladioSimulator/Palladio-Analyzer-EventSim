package edu.kit.ipd.sdq.eventsim.launch;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.middleware.MeasurementStorageModule;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddlewareModule;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModule;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModuleRegistry;

public class DefaultSimulationModule extends AbstractModule {

    private static final Logger logger = Logger.getLogger(DefaultSimulationModule.class);

    private ISimulationConfiguration config;

    private InstrumentationDescription instrumentationDescription;

    public DefaultSimulationModule(ISimulationConfiguration config,
            InstrumentationDescription instrumentationDescription) {
        this.config = config;
        this.instrumentationDescription = instrumentationDescription;
    }

    @Override
    protected void configure() {
        SimulationModuleRegistry moduleRegistry = SimulationModuleRegistry.createFrom(Platform.getExtensionRegistry());
        bind(SimulationModuleRegistry.class).toInstance(moduleRegistry);

        install(new SimulationMiddlewareModule(config));
        install(new MeasurementStorageModule(config));

        // install Guice modules of extensions, starting with extensions of lower priority
        for (SimulationModule m : moduleRegistry.getModules()) {
            logger.info("Installing simulation module " + m.getName() + " (" + m.getId() + "), priority = "
                    + m.getPriority());
            if (m.getGuiceModule() != null) {
                install(m.getGuiceModule());
            }
        }

        bind(ISimulationConfiguration.class).toInstance(config);
        bind(PCMModel.class).toInstance(config.getPCMModel());
        bind(InstrumentationDescription.class).toInstance(instrumentationDescription);

    }

}

package edu.kit.ipd.sdq.eventsim.launch;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddlewareModule;
import edu.kit.ipd.sdq.eventsim.resources.EventSimResourceModule;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystemModule;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModule;

public class DefaultSimulationModule extends AbstractModule {

    private ISimulationConfiguration config;
    
    private InstrumentationDescription instrumentationDescription;

    public DefaultSimulationModule(ISimulationConfiguration config, InstrumentationDescription instrumentationDescription) {
        this.config = config;
        this.instrumentationDescription = instrumentationDescription;
    }

    @Override
    protected void configure() {
        install(new SimulationMiddlewareModule(config));
        install(new EventSimWorkloadModule());
        install(new EventSimSystemModule());
        install(new EventSimResourceModule());
        

        bind(ISimulationConfiguration.class).toInstance(config);
        bind(PCMModel.class).toInstance(config.getPCMModel());
        bind(InstrumentationDescription.class).toInstance(instrumentationDescription);
        
    }

}

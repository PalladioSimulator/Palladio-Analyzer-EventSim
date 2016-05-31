package edu.kit.ipd.sdq.eventsim.workload.tests;

import org.mockito.Mockito;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddlewareModule;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystemModule;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModule;

public class TestSimulationModule extends AbstractModule {

	private ISimulationConfiguration config;

	public TestSimulationModule(ISimulationConfiguration config) {
		this.config = config;
	}

	@Override
	protected void configure() {
        install(new SimulationMiddlewareModule(config));
		install(new EventSimWorkloadModule());
		install(new EventSimSystemModule());

		bind(IActiveResource.class).toInstance(Mockito.mock(IActiveResource.class));
		bind(IPassiveResource.class).toInstance(Mockito.mock(IPassiveResource.class));

		MeasurementStorage measurementStorage = Mockito.mock(MeasurementStorage.class);
		bind(MeasurementStorage.class).toInstance(measurementStorage);

//		ISimulationMiddleware middleware = new SimulationMiddleware(config, measurementStorage);
//		bind(ISimulationMiddleware.class).toInstance(middleware);
		
        bind(ISimulationConfiguration.class).toInstance(config);
        bind(PCMModel.class).toInstance(config.getPCMModel());
        bind(InstrumentationDescription.class).toInstance(Mockito.mock(InstrumentationDescription.class));
	}

}

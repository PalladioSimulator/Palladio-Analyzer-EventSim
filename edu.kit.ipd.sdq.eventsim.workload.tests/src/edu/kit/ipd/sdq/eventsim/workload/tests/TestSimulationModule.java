package edu.kit.ipd.sdq.eventsim.workload.tests;

import org.mockito.Mockito;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystem;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkload;

public class TestSimulationModule extends AbstractModule {

	private ISimulationConfiguration config;

	public TestSimulationModule(ISimulationConfiguration config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		install(new EventSimWorkload());
		install(new EventSimSystem());
		
		bind(IActiveResource.class).toInstance(Mockito.mock(IActiveResource.class));
		bind(IPassiveResource.class).toInstance(Mockito.mock(IPassiveResource.class));

		ISimulationMiddleware middleware = new SimulationMiddleware(config);
		bind(ISimulationMiddleware.class).toInstance(middleware);

		bind(MeasurementStorage.class).toInstance(Mockito.mock(MeasurementStorage.class));
	}

}

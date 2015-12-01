package edu.kit.ipd.sdq.eventsim.workload;

import org.mockito.Mockito;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddleware;

public class TestSimulationModule extends AbstractModule {

	private ISimulationConfiguration config;

	public TestSimulationModule(ISimulationConfiguration config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		install(new EventSimWorkload());

		bind(ISystem.class).toInstance(Mockito.mock(ISystem.class));
		bind(IActiveResource.class).toInstance(Mockito.mock(IActiveResource.class));
		bind(IPassiveResource.class).toInstance(Mockito.mock(IPassiveResource.class));

		ISimulationMiddleware middleware = new SimulationMiddleware(config);
		bind(ISimulationMiddleware.class).toInstance(middleware);

		bind(MeasurementStorage.class).toInstance(Mockito.mock(MeasurementStorage.class));
	}

}

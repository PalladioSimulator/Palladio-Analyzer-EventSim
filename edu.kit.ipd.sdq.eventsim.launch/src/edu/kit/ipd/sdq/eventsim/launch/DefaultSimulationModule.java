package edu.kit.ipd.sdq.eventsim.launch;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.resources.EventSimResource;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystem;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkload;

public class DefaultSimulationModule extends AbstractModule {

	private ISimulationConfiguration config;

	public DefaultSimulationModule(ISimulationConfiguration config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		install(new EventSimWorkload());
		install(new EventSimSystem());
		install(new EventSimResource());

		// bind measurement storage; currently fixed to RMeasurementStore
		MeasurementStorage measurementStorage = RMeasurementStore.fromLaunchConfiguration(config.getConfigurationMap());
		if (measurementStorage == null) {
			throw new RuntimeException("R measurement store could not bet constructed from launch configuration.");
		}
		bind(MeasurementStorage.class).toInstance(measurementStorage);

		// bind middleware to single instance to ensure that all simulation components use the same instance
		ISimulationMiddleware middleware = new SimulationMiddleware(config, measurementStorage);
		bind(ISimulationMiddleware.class).toInstance(middleware);
	}

}

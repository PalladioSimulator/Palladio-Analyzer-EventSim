package edu.kit.ipd.sdq.eventsim.middleware;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;

public class MeasurementStorageModule extends AbstractModule {

    private ISimulationConfiguration config;

    public MeasurementStorageModule(ISimulationConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        // nothing to do
    }

    @Provides
    @Singleton
    public MeasurementStorage createMeasurementStorage() {
        // bind measurement storage; currently fixed to RMeasurementStore
        MeasurementStorage measurementStorage = RMeasurementStore.fromLaunchConfiguration(config.getConfigurationMap());
        if (measurementStorage == null) {
            throw new RuntimeException("R measurement store could not bet constructed from launch configuration.");
        }
        return measurementStorage;
    }

}

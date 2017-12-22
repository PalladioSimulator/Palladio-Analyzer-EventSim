package edu.kit.ipd.sdq.eventsim.middleware;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;

public class MeasurementStorageModule extends AbstractModule {

    private ISimulationConfiguration config;

    public MeasurementStorageModule(ISimulationConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        // lookup Rserve connection
        RserveConnection connection = ConnectionRegistry.instance().getConnection();
        MeasurementStorage measurementStorage = RMeasurementStore.fromLaunchConfiguration(config.getConfigurationMap(),
                connection);
        if (measurementStorage == null) {
            throw new RuntimeException("R measurement store could not bet constructed from launch configuration.");
        }
        bind(MeasurementStorage.class).toInstance(measurementStorage);
    }

}

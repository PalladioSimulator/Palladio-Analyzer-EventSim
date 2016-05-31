package edu.kit.ipd.sdq.eventsim.middleware;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simucomframework.SimuComDefaultRandomNumberGenerator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.SimulationModel;

public class SimulationMiddlewareModule extends AbstractModule {

    private ISimulationConfiguration config;

    public SimulationMiddlewareModule(ISimulationConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        // bind measurement storage; currently fixed to RMeasurementStore
        MeasurementStorage measurementStorage = RMeasurementStore.fromLaunchConfiguration(config.getConfigurationMap());
        if (measurementStorage == null) {
            throw new RuntimeException("R measurement store could not bet constructed from launch configuration.");
        }
        bind(MeasurementStorage.class).toInstance(measurementStorage);

        bind(ISimulationMiddleware.class).to(SimulationMiddleware.class).in(Singleton.class);
        bind(ISimulationModel.class).to(SimulationModel.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public IRandomGenerator createRandomGenerator() {
        return new SimuComDefaultRandomNumberGenerator(config.getRandomSeed());
    }

}

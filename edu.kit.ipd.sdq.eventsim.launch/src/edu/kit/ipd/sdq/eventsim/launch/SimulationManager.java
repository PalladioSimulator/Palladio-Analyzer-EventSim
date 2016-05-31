package edu.kit.ipd.sdq.eventsim.launch;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simulation.IStatusObserver;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;

public class SimulationManager {

    private final IWorkload workload;

    private final ISystem system;

    private final ISimulationMiddleware middleware;

    private final MeasurementStorage measurementStorage;

    @Inject
    public SimulationManager(IWorkload workload, ISystem system, ISimulationMiddleware middleware,
            MeasurementStorage measurementStorage) {
        this.workload = workload;
        this.system = system;
        this.middleware = middleware;
        this.measurementStorage = measurementStorage;
    }

    public void startSimulation() {
        startSimulation(new IStatusObserver() {
            @Override
            public void updateStatus(int percentDone, double currentSimTime, long measurementsTaken) {
                // do nothing
            }
        });
    }

    public void startSimulation(IStatusObserver statusObserver) {
        middleware.startSimulation(statusObserver);
    }

    public IWorkload getWorkload() {
        return workload;
    }

    public ISystem getSystem() {
        return system;
    }

    public ISimulationMiddleware getMiddleware() {
        return middleware;
    }

    public MeasurementStorage getMeasurementStorage() {
        return measurementStorage;
    }

}

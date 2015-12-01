package edu.kit.ipd.sdq.eventsim.launch;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simulation.IStatusObserver;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;

public class SimulationManager {

	private final IWorkload workload;

	private final ISimulationMiddleware middleware;

	private final MeasurementStorage measurementStorage;

	@Inject
	public SimulationManager(IWorkload workload, ISimulationMiddleware middleware,
			MeasurementStorage measurementStorage) {
		this.workload = workload;
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
		workload.generate();
		middleware.startSimulation(statusObserver);

		// when simulation has stopped...
		measurementStorage.finish();
	}

}

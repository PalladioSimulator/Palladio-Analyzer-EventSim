package edu.kit.ipd.sdq.eventsim.workload;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;

public class WorkloadMeasurementConfiguration implements ProbeConfiguration {

    private ISimulationModel simulationModel;
    
    private EventSimWorkloadModel workloadModel;
    
    private ISimulationMiddleware middleware;

    public WorkloadMeasurementConfiguration(EventSimWorkloadModel workloadModel, ISimulationMiddleware middleware, ISimulationModel simulationModel) {
        this.workloadModel = workloadModel;
        this.middleware = middleware;
        this.simulationModel = simulationModel;
    }
    
    public EventSimWorkloadModel getWorkloadModel() {
        return workloadModel;
    }
    
    public ISimulationMiddleware getMiddleware() {
        return middleware;
    }
    
    public ISimulationModel getSimulationModel() {
        return simulationModel;
    }

}

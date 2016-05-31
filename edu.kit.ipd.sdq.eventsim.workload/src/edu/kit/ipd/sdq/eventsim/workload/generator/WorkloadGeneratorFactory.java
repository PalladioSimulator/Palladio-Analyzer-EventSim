package edu.kit.ipd.sdq.eventsim.workload.generator;

import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.OpenWorkload;

public interface WorkloadGeneratorFactory {

    ClosedWorkloadGenerator createClosed(ClosedWorkload workload);
    
    OpenWorkloadGenerator createOpen(OpenWorkload workload);
    
}

package edu.kit.ipd.sdq.eventsim.workload.entities;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public interface UserFactory {

    public User create(UsageScenario scenario);
    
}

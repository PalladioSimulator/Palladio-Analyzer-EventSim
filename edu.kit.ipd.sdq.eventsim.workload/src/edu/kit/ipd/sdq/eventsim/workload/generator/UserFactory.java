package edu.kit.ipd.sdq.eventsim.workload.generator;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import edu.kit.ipd.sdq.eventsim.workload.entities.User;

public interface UserFactory {

    public User create(UsageScenario scenario);
    
}

package edu.kit.ipd.sdq.eventsim.resources.entities;

import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;

import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.scheduler.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.resources.SchedulingPolicy;

public interface SimResourceFactory {

    SimActiveResource createActiveResource(IActiveResource resource, String processingRate, int numberOfInstances,
            SchedulingPolicy schedulingStrategy, ProcessingResourceSpecification specification);

    SimLinkingResource createLinkingResource(IActiveResource resource, @Assisted("latency") String latencySpecification,
            @Assisted("throughput") String throughputSpecification);

    SimPassiveResource createPassiveResource(IPassiveResource resource, PassiveResource specification);

}

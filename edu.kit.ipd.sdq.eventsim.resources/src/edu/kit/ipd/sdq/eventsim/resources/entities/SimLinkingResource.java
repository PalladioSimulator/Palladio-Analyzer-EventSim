package edu.kit.ipd.sdq.eventsim.resources.entities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.resources.SchedulingPolicy;

public class SimLinkingResource extends SimActiveResource {

    private String latencySpecification;

    @Inject
    public SimLinkingResource(ISimulationModel model, @Assisted IActiveResource resource,
            @Assisted("latency") String latencySpecification, @Assisted("throughput") String throughputSpecification) {
        // TODO SimLinkingResource resource should not inherit SimActiveResource. Use composition!
        super(model, resource, throughputSpecification, 1, SchedulingPolicy.FCFS, null);
        this.latencySpecification = latencySpecification;
    }

    @Override
    protected double calculateConcreteDemand(double abstractDemand) {
        // use calculation routine from super-class, but add latency
        return super.calculateConcreteDemand(abstractDemand)
                + Context.evaluateStatic(latencySpecification, Double.class);
    }

}

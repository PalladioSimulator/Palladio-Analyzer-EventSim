package edu.kit.ipd.sdq.eventsim.resources.entities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;

public class SimLinkingResource extends SimActiveResource {

    private String latencySpecification;

    @Inject
    public SimLinkingResource(ISimulationModel model, @Assisted IActiveResource resource,
            @Assisted("latency") String latencySpecification, @Assisted("throughput") String throughputSpecification) {
        // TODO SimLinkingResource resource should not inherit SimActiveResource. Use composition!
        super(model, resource, throughputSpecification, 1, null, null); // TODO pass FCFS resource
                                                                        // specification!
        this.latencySpecification = latencySpecification;
    }

    @Override
    protected double calculateConcreteDemand(double abstractDemand, int resourceServiceId) {
        // use calculation routine from super-class, but add latency
        return super.calculateConcreteDemand(abstractDemand, resourceServiceId)
                + Context.evaluateStatic(latencySpecification, Double.class);
    }

}

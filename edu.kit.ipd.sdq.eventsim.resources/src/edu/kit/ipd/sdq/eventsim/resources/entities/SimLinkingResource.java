package edu.kit.ipd.sdq.eventsim.resources.entities;

import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

public class SimLinkingResource extends AbstractActiveResource {

    private String latencySpecification;

    private LinkingResource specification;

    @Inject
    public SimLinkingResource(ISimulationModel model, @Assisted IActiveResource resource,
            @Assisted("latency") String latencySpecification, @Assisted("throughput") String throughputSpecification,
            @Assisted LinkingResource specification) {
        super(model, "SimLinkingResource", resource, null, 1);

        this.latencySpecification = latencySpecification;
        this.specification = specification;
    }

    public LinkingResource getSpecification() {
        return specification;
    }

    @Override
    protected double calculateConcreteDemand(double abstractDemand, int resourceServiceId) {
        // TODO consider throughput
        return Context.evaluateStatic(latencySpecification, Double.class);
    }

    /**
     * Returns the resource ID.
     * 
     * @return the resource's ID
     * 
     * @see IActiveResource#getId()
     */
    public String getId() {
        return specification.getId();
    }

    /**
     * Returns the name of the resource.
     * 
     * @return the resource's name
     */
    public String getName() {
        return specification.getEntityName() + " [LAN]";
    }

}

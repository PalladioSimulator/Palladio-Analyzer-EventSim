package edu.kit.ipd.sdq.eventsim.resources.entities;

import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

public class SimLinkingResource extends AbstractActiveResource {

    private String latencySpecification;

    private String throughputSpecification;

    private LinkingResource specification;

    @Inject
    public SimLinkingResource(ISimulationModel model, @Assisted IActiveResource resource,
            @Assisted("latency") String latencySpecification, @Assisted("throughput") String throughputSpecification,
            @Assisted LinkingResource specification) {
        super(model, "SimLinkingResource", resource, null, 1);

        this.latencySpecification = latencySpecification;
        this.throughputSpecification = throughputSpecification;
        this.specification = specification;
    }

    public LinkingResource getSpecification() {
        return specification;
    }

    @Override
    protected double calculateConcreteDemand(double abstractDemand, int resourceServiceId) {
        double throughput = StackContext.evaluateStatic(throughputSpecification, Double.class);
        if (throughput <= 0) {
            throw new EventSimException(String.format("Thoughput must be greater than 0, but is %s for %s", throughput,
                    PCMEntityHelper.toString(specification)));
        }
        double latency = StackContext.evaluateStatic(latencySpecification, Double.class);
        double concreteDemand = latency + abstractDemand / throughput;
        return concreteDemand;
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

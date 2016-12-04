package edu.kit.ipd.sdq.eventsim.resources.entities;

import org.omg.CORBA.Request;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.resourcetype.SchedulingPolicy;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;

/**
 * An active resource can process demands of {@link Request}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class SimActiveResource extends AbstractActiveResource {

    private String processingRate;

    private ProcessingResourceSpecification specification;

    /**
     * Constructs an active resource that wraps the specified resource.
     * 
     * @param model
     *            the simulation model
     * @param resource
     *            the wrapped scheduler resource
     * @param processingRate
     * @param numberOfInstances
     * @param specification
     */
    @Inject
    public SimActiveResource(ISimulationModel model, @Assisted IActiveResource resource,
            @Assisted String processingRate, @Assisted int numberOfInstances,
            @Assisted SchedulingPolicy schedulingStrategy, @Assisted ProcessingResourceSpecification specification) {
        super(model, "SimActiveResource", resource, schedulingStrategy, numberOfInstances);

        this.processingRate = processingRate;
        this.specification = specification;
    }

    @Override
    protected double calculateConcreteDemand(double abstractDemand) {
        return abstractDemand / Context.evaluateStatic(processingRate, Double.class);
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
     * 
     * @see IActiveResource#getName()
     */
    public String getName() {
        // obtain entity name (HDD, CPU, ...) from specification
        String resourceContainerName = specification.getResourceContainer_ProcessingResourceSpecification()
                .getEntityName();
        String resourceTypeName = specification.getActiveResourceType_ActiveResourceSpecification().getEntityName();
        return resourceContainerName + " [" + resourceTypeName + "]";
    }

    public ResourceContainer getResourceContainer() {
        return specification.getResourceContainer_ProcessingResourceSpecification();
    }

    public ProcessingResourceType getResourceType() {
        return specification.getActiveResourceType_ActiveResourceSpecification();
    }

}

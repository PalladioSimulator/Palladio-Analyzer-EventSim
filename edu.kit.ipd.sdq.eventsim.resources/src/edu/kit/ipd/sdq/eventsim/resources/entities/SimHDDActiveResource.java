package edu.kit.ipd.sdq.eventsim.resources.entities;

import org.palladiosimulator.pcm.resourceenvironment.HDDProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourcetype.SchedulingPolicy;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;

/**
 * An active resource which can process HDD read and write requests.
 * 
 * @author Thomas Zwickl
 * @author Philipp Merkle
 */
public class SimHDDActiveResource extends SimActiveResource {

    private final String writeProcessingRate;
    private final String readProcessingRate;

    private final int READ_SERVICE_ID = 1;
    private final int WRITE_SERVICE_ID = 2;

    @Inject
    public SimHDDActiveResource(final ISimulationModel model, @Assisted final IActiveResource resource,
            @Assisted final String processingRate, @Assisted final int numberOfInstances,
            @Assisted final SchedulingPolicy schedulingStrategy,
            @Assisted final HDDProcessingResourceSpecification specification,
            @Assisted("writeProcessingRate") final String writeProcessingRate,
            @Assisted("readProcessingRate") final String readProcessingRate) {
        super(model, resource, processingRate, numberOfInstances, schedulingStrategy, specification);
        this.writeProcessingRate = writeProcessingRate;
        this.readProcessingRate = readProcessingRate;
    }

    @Override
    protected double calculateConcreteDemand(double abstractDemand, int resourceServiceId) {
        double concreteDemand;
        if (resourceServiceId == this.READ_SERVICE_ID) {
            concreteDemand = abstractDemand / Context.evaluateStatic(this.readProcessingRate, Double.class);
        } else if (resourceServiceId == this.WRITE_SERVICE_ID) {
            concreteDemand = abstractDemand / Context.evaluateStatic(this.writeProcessingRate, Double.class);
        } else {
            throw new IllegalStateException("HDD Resource called without explicit read/write call");
        }
        return concreteDemand;
    }

}

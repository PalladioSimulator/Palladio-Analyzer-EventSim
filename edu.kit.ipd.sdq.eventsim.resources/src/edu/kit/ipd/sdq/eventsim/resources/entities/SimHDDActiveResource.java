package edu.kit.ipd.sdq.eventsim.resources.entities;

import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourcetype.SchedulingPolicy;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.Procedure;

/**
 * An active resource which can process HDD read and write requests.
 * 
 * @author Thomas Zwickl
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
            @Assisted final ProcessingResourceSpecification specification,
            @Assisted("writeProcessingRate") final String writeProcessingRate,
            @Assisted("readProcessingRate") final String readProcessingRate) {
        super(model, resource, processingRate, numberOfInstances, schedulingStrategy, specification);
        this.writeProcessingRate = writeProcessingRate;
        this.readProcessingRate = readProcessingRate;
    }

    @Override
    public void consumeResource(final SimulatedProcess process, final double abstractDemand,
            final int resourceServiceID, Procedure onServedCallback) {
        double currentDemand;
        if (resourceServiceID == this.READ_SERVICE_ID) {
            currentDemand = abstractDemand / Context.evaluateStatic(this.readProcessingRate, Double.class);
        } else if (resourceServiceID == this.WRITE_SERVICE_ID) {
            currentDemand = abstractDemand / Context.evaluateStatic(this.writeProcessingRate, Double.class);
        } else {
            throw new IllegalStateException("HDD Resource called without explicit read/write call");
        }
        super.consumeResource(process, currentDemand, resourceServiceID, onServedCallback);
    }
}

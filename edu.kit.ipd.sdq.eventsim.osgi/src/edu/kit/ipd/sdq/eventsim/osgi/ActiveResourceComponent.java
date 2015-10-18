package edu.kit.ipd.sdq.eventsim.osgi;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.resources.EventSimActiveResource;

@Component(factory = "activeresource.factory")
public class ActiveResourceComponent implements IActiveResource {

	private IActiveResource resourceDelegate;
	
	private int simulationId;

	private ISimulationManager compositionManager;
	
	@Activate
	void activate(ComponentContext ctx) {
		simulationId = (int) ctx.getProperties().get(SimulationManager.SIMULATION_ID);
		
		resourceDelegate = new EventSimActiveResource(compositionManager.getMiddleware(simulationId));
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality=ReferenceCardinality.OPTIONAL)
	public void bindCompositionManager(ISimulationManager manager) {
		this.compositionManager = manager;
	}
	
	public void unbindCompositionManager(ISimulationManager manager) {
		this.compositionManager = null;
	}

	public void consume(IRequest request, ResourceContainer resourceContainer, ResourceType resourceType,
			double absoluteDemand) {
		resourceDelegate.consume(request, resourceContainer, resourceType, absoluteDemand);
	}
	
}

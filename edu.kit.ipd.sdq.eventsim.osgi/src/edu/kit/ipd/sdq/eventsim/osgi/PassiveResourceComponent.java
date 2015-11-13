package edu.kit.ipd.sdq.eventsim.osgi;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.resources.EventSimPassiveResource;

@Component(factory = "passiveresource.factory")
public class PassiveResourceComponent implements IPassiveResource {

	private static final Logger log = Logger.getLogger(PassiveResourceComponent.class);
	
	private IPassiveResource resourceDelegate;

	private int simulationId;

	private ISimulationManager compositionManager;

	@Activate
	void activate(ComponentContext ctx) {
		simulationId = (int) ctx.getProperties().get(SimulationManager.SIMULATION_ID);

		resourceDelegate = new EventSimPassiveResource(compositionManager.getMiddleware(simulationId));
	}
	
	@Deactivate
	void deactivate() {
		log.debug("Deactivated passive resource simulation component (Simulation ID = " + simulationId + ")");
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	public void bindCompositionManager(ISimulationManager manager) {
		this.compositionManager = manager;
	}

	public void unbindCompositionManager(ISimulationManager manager) {
		this.compositionManager = null;
	}

	public boolean acquire(IRequest request, AssemblyContext ctx, PassiveResource passiveResouce, int num) {
		return resourceDelegate.acquire(request, ctx, passiveResouce, num);
	}

	public void release(IRequest request, AssemblyContext ctx, PassiveResource passiveResouce, int num) {
		resourceDelegate.release(request, ctx, passiveResouce, num);
	}

}

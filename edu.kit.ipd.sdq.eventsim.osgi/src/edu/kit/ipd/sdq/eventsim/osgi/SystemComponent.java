package edu.kit.ipd.sdq.eventsim.osgi;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystem;

@Component(factory = "system.factory")
public class SystemComponent implements ISystem {

	private ComponentFactory activeResourceFactory;

	private ComponentInstance activeResourceInstance;

	private ComponentFactory passiveResourceFactory;

	private ComponentInstance passiveResourceInstance;

	private int simulationId;

	private ISystem systemDelegate;

	private ISimulationManager compositionManager;

	private IActiveResource activeResource;

	private IPassiveResource passiveResource;

	@Activate
	void activate(ComponentContext ctx) {
		simulationId = (int) ctx.getProperties().get(SimulationManager.SIMULATION_ID);

		// instantiate active resource component
		activeResourceInstance = activeResourceFactory.newInstance(ctx.getProperties());
		activeResource = (IActiveResource) activeResourceInstance.getInstance();

		// instantiate passive resource component
		passiveResourceInstance = passiveResourceFactory.newInstance(ctx.getProperties());
		passiveResource = (IPassiveResource) passiveResourceInstance.getInstance();

		// delegate invocations of the ISystem interface to the system delegate
		systemDelegate = new EventSimSystem(compositionManager.getMiddleware(simulationId));

		// register callbacks for redirecting calls to required services
		systemDelegate.onActiveResourceDemand((request, resourceContainer, resourceType, demand) -> activeResource
				.consume(request, resourceContainer, resourceType, demand));
		systemDelegate.onPassiveResourceAcquire(
				(request, asctx, passiveResouce, num) -> passiveResource.acquire(request, asctx, passiveResouce, num));
		systemDelegate.onPassiveResourceRelease(
				(request, asctx, passiveResouce, num) -> passiveResource.release(request, asctx, passiveResouce, num));
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	public void bindCompositionManager(ISimulationManager manager) {
		this.compositionManager = manager;
	}

	public void unbindCompositionManager(ISimulationManager manager) {
		this.compositionManager = null;
	}

	@Reference(target = "(component.factory=activeresource.factory)")
	public void bindActiveResourceFactory(final ComponentFactory factory) {
		this.activeResourceFactory = factory;
	}

	public void unbindActiveResourceFactory(final ComponentFactory factory) {
		activeResourceInstance.dispose();
		activeResource = null;
	}

	@Reference(target = "(component.factory=passiveresource.factory)")
	public void bindPassiveResourceFactory(final ComponentFactory factory) {
		this.passiveResourceFactory = factory;
	}

	public void unbindPassiveResourceFactory(final ComponentFactory factory) {
		passiveResourceInstance.dispose();
		passiveResource = null;
	}

	public void callService(IUser user, EntryLevelSystemCall call) {
		systemDelegate.callService(user, call);
	}

	public void onActiveResourceDemand(ActiveResourceListener callback) {
		systemDelegate.onActiveResourceDemand(callback);
	}

	public void onPassiveResourceAcquire(PassiveResourceAcquireListener callback) {
		systemDelegate.onPassiveResourceAcquire(callback);
	}

	public void onPassiveResourceRelease(PassiveResourceReleaseListener callback) {
		systemDelegate.onPassiveResourceRelease(callback);
	}

}

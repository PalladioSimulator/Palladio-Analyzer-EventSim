package edu.kit.ipd.sdq.eventsim.osgi;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkload;

@Component(factory = "workload.factory")
public class WorkloadComponent implements IWorkload {

	private static final Logger log = Logger.getLogger(WorkloadComponent.class);
	
	private ComponentFactory systemFactory;

	private ComponentInstance systemInstance;

	private ISystem system;

	private int simulationId;

	private ISimulationManager compositionManager;

	private IWorkload workloadDelegate;

	@Activate
	void activate(ComponentContext ctx) {
		simulationId = (int) ctx.getProperties().get(SimulationManager.SIMULATION_ID);

		// instantiate system component
		systemInstance = systemFactory.newInstance(ctx.getProperties());
		system = (ISystem) systemInstance.getInstance();

		// delegate invocations of the IWorkload interface to the workload delegate
		workloadDelegate = new EventSimWorkload(compositionManager.getMiddleware(simulationId));

		// register callbacks for redirecting calls to required services
		workloadDelegate.onSystemCall((user, call) -> system.callService(user, call));
	}
	
	@Deactivate
	public void deactivate() {
		log.debug("Deactivated workload simulation component (Simulation ID = " + simulationId + ")");
	}

	@Reference(target = "(component.factory=system.factory)")
	public void bindSytemFactory(final ComponentFactory factory) {
		this.systemFactory = factory;
	}

	public void unbindSytemFactory(final ComponentFactory factory) {
		systemInstance.dispose();
		system = null;
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	public void bindCompositionManager(ISimulationManager manager) {
		this.compositionManager = manager;
	}

	public void unbindCompositionManager(ISimulationManager manager) {
		this.compositionManager = null;
	}

	public void generate() {
		workloadDelegate.generate();
	}

	public void onSystemCall(SystemCallListener callback) {
		workloadDelegate.onSystemCall(callback);
	}

}

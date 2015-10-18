package edu.kit.ipd.sdq.eventsim.osgi;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;

@Component
public class SimulationManager implements ISimulationManager {

	public static final String SIMULATION_ID = "simulation.id";

	private ComponentFactory workloadFactory;

	private ComponentInstance workloadInstance;

	private ComponentFactory middlewareFactory;

	private ComponentInstance middlewareInstance;

	private ISimulationMiddleware middleware;

	private ComponentContext ctx;

	@Activate
	void activate(ComponentContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int prepareSimulation(SimulationConfiguration config) {
		// TODO support multiple simulation ids
		int simulationId = 0;

		// instantiate middleware component
		final Dictionary properties = new Hashtable<>();
		properties.put(SIMULATION_ID, simulationId);
		// props.put("SytemFactory.target", "(component.factory=system.factory.b)");
		properties.put("WorkloadFactory.target", "(component.factory=workload.factory)");

		middlewareInstance = middlewareFactory.newInstance(properties);
		middleware = (ISimulationMiddleware) middlewareInstance.getInstance();
		middleware.initialize(config, config.getPCMModel());

		// register middleware as service
		final Dictionary serviceProperties = new Hashtable<>();
		serviceProperties.put(ComponentConstants.COMPONENT_NAME, ISimulationMiddleware.class.getName());
		serviceProperties.put(SIMULATION_ID, simulationId);
		ServiceRegistration<ISimulationMiddleware> reg = ctx.getBundleContext()
				.registerService(ISimulationMiddleware.class, middleware, serviceProperties);

		// instantiate workload component
		workloadInstance = workloadFactory.newInstance(properties);
		WorkloadComponent workload = (WorkloadComponent) workloadInstance.getInstance();
		// workload listens for simulation start event and must not be started here

		return simulationId;
	}

	@Reference(target = "(component.factory=workload.factory)")
	public void bindWorkloadFactory(final ComponentFactory factory) {
		this.workloadFactory = factory;
	}

	public void unbindWorkloadFactory(final ComponentFactory factory) {
		workloadInstance.dispose();
	}

	@Reference(target = "(component.factory=middleware.factory)")
	public void bindMiddlewareFactory(final ComponentFactory factory) {
		this.middlewareFactory = factory;
	}

	public void unbindMiddlewareFactory(final ComponentFactory factory) {
		middlewareInstance.dispose();
	}

	@Override
	public ISimulationMiddleware getMiddleware(int simulationId) {
		try {
			Collection<ServiceReference<ISimulationMiddleware>> refs = ctx.getBundleContext()
					.getServiceReferences(ISimulationMiddleware.class, "(&(simulation.id=" + simulationId
							+ ")(component.name=" + ISimulationMiddleware.class.getName() + "))");
			// TODO could be more than one
			ServiceReference<ISimulationMiddleware> ref = refs.iterator().next();
			return middleware = ctx.getBundleContext().getService(ref);

		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
			return null; // TODO
		}
	}

}

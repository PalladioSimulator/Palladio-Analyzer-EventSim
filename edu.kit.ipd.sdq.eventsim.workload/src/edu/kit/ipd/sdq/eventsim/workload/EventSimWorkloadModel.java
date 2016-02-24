package edu.kit.ipd.sdq.eventsim.workload;

import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.probfunction.math.IProbabilityFunctionFactory;
import de.uka.ipd.sdq.probfunction.math.impl.ProbabilityFunctionFactoryImpl;
import de.uka.ipd.sdq.simucomframework.variables.cache.StoExCache;
import edu.kit.ipd.sdq.eventsim.AbstractEventSimModel;
import edu.kit.ipd.sdq.eventsim.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.api.events.SystemRequestFinishedEvent;
import edu.kit.ipd.sdq.eventsim.api.events.WorkloadUserFinishedEvent;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.Instrumentor;
import edu.kit.ipd.sdq.eventsim.instrumentation.injection.InstrumentorBuilder;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.workload.debug.DebugUsageTraversalListener;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.events.ResumeUsageTraversalEvent;
import edu.kit.ipd.sdq.eventsim.workload.generator.BuildWorkloadGenerator;
import edu.kit.ipd.sdq.eventsim.workload.generator.IWorkloadGenerator;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.UsageBehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.UsageInterpreterConfiguration;

/**
 * The EventSim workload simulation model. This is the central class of the workload simulation.
 * 
 * run. Before the simulation starts, it initialises the simulation in the {@code init()} method. During the simulation,
 * it provides information about the PCM model that is to be simulated, the simulation configuration and the simulation
 * status. Finally, it cleans up after a simulation run in the {finalise()} method.
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 * 
 */
@Singleton
public class EventSimWorkloadModel extends AbstractEventSimModel implements IWorkload {

	private static final Logger logger = Logger.getLogger(EventSimWorkloadModel.class);

	private UsageBehaviourInterpreter usageInterpreter;

	private MeasurementFacade<WorkloadMeasurementConfiguration> measurementFacade;
	
	private ISystem system;
	
	@Inject
	public EventSimWorkloadModel(ISystem system, ISimulationMiddleware middleware,
			MeasurementStorage measurementStorage) {
		super(middleware, measurementStorage);
		this.system = system;
		init();
	}
	
	/**
	 * This method prepares the EventSim workload simulator and creates the initial events to start the workload
	 * generation.
	 */
	private void init() {		
		// initialise behaviour interpreters
		usageInterpreter = new UsageBehaviourInterpreter(new UsageInterpreterConfiguration());
	
		// initialise probfunction factory and random generator
		IProbabilityFunctionFactory probFunctionFactory = ProbabilityFunctionFactoryImpl.getInstance();
		probFunctionFactory.setRandomGenerator(this.getSimulationMiddleware().getRandomGenerator());
		StoExCache.initialiseStoExCache(probFunctionFactory);
	
		// install debug traversal listeners, if debugging is enabled
		if (logger.isDebugEnabled()) {
			DebugUsageTraversalListener.install(this.usageInterpreter.getConfiguration());
		}
	
		setupMeasurements();
	
		registerEventHandler();
	}

	@Override
	public void generate() {
		// start the simulation by generating the workload
		final List<IWorkloadGenerator> workloadGenerators = this.execute(new BuildWorkloadGenerator(this));
		for (final IWorkloadGenerator d : workloadGenerators) {
			d.processWorkload();
		}
	}
	
	/**
	 * Register event handler to react on specific simulation events.
	 */
	private void registerEventHandler() {
		ISimulationMiddleware middleware = getSimulationMiddleware();
		middleware.registerEventHandler(WorkloadUserFinishedEvent.class,
				e -> middleware.increaseMeasurementCount());
		
		// TODO perhaps move to EntryLevelSystemCallTraversalStrategy
		// setup system processed request event listener
		this.getSimulationMiddleware().registerEventHandler(SystemRequestFinishedEvent.class, event -> {
			IRequest request = event.getRequest();
			User user = (User) request.getUser();
			new ResumeUsageTraversalEvent(EventSimWorkloadModel.this, user.getUserState()).schedule(user, 0);
		});
	}

	private void setupMeasurements() {
		// create instrumentor for instrumentation description
		// TODO get rid of cast (and middleware/simulation dependencies)
		SimulationConfiguration config = (SimulationConfiguration) getSimulationMiddleware().getSimulationConfiguration();
		Instrumentor<?, ?> instrumentor = InstrumentorBuilder
				.buildFor(config.getPCMModel())
				.inBundle(Activator.getContext().getBundle())
				.withDescription(config.getInstrumentationDescription())
				.withStorage(getMeasurementStorage())
				.forModelType(UserActionRepresentative.class)
				.withoutMapping()
				.createFor(WorkloadMeasurementConfiguration.from(this));
		instrumentor.instrumentAll();

		MeasurementStorage measurementStorage = getMeasurementStorage();
		measurementStorage.addIdExtractor(User.class, c -> Long.toString(((User)c).getEntityId()));
		measurementStorage.addNameExtractor(User.class, c -> ((User)c).getName());
		measurementStorage.addIdExtractor(AbstractUserAction.class, c -> ((AbstractUserAction)c).getId());
		measurementStorage.addNameExtractor(AbstractUserAction.class, c -> ((AbstractUserAction)c).getEntityName());
		
//		// initialize measurement facade
//		measurementFacade = new MeasurementFacade<>(WorkloadMeasurementConfiguration.from(this),
//				new BundleProbeLocator<>(Activator.getContext().getBundle()));
//
//		// response time of system calls
//		execute(new FindAllUserActionsByType<>(EntryLevelSystemCall.class)).forEach(
//				call -> measurementFacade
//						.createCalculator(new TimeSpanBetweenUserActionsCalculator("RESPONSE_TIME"))
//						.from(call, "before").to(call, "after")
//						.forEachMeasurement(m -> measurementStorage.putPair(m)));
//
//		// response time of usage scenarios
//		execute(new FindUsageScenarios()).forEach(scenario -> {
//			// TODO recursive vs. non-recursive
//				Start start = execute(new FindActionsInUsageScenario<>(scenario, Start.class, false)).get(0);
//				Stop stop = execute(new FindActionsInUsageScenario<>(scenario, Stop.class, false)).get(0);
//				measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator("RESPONSE_TIME"))
//						.from(start, "before").to(stop, "after")
//						.forEachMeasurement(m -> measurementStorage.putPair(m));
//				// TODO redefine measurement point (Start/Stop --> UsageScenario)
//			});

	}

	@Override
	public void finalise() {
		super.finalise();

		// TODO required?
		measurementFacade = null;
	}

	/**
	 * Gives access to the usage behavior interpreter
	 * 
	 * @return A usage behavior interpreter
	 */
	public UsageBehaviourInterpreter getUsageInterpreter() {
		return usageInterpreter;
	}
	
	public ISystem getSystem() {
		return system;
	}
	
	public MeasurementFacade<WorkloadMeasurementConfiguration> getMeasurementFacade() {
		return measurementFacade;
	}

}

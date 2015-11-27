package edu.kit.ipd.sdq.eventsim.launch.workflow.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.analyzer.workflow.blackboard.PCMResourceSetPartition;
import org.palladiosimulator.analyzer.workflow.jobs.LoadPCMModelsIntoBlackboardJob;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import de.uka.ipd.sdq.codegen.simucontroller.dockmodel.DockModel;
import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.launch.SimulationDockWrapper;
import edu.kit.ipd.sdq.eventsim.launch.runconfig.SimulationComponentWorkflowConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.middleware.SimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.resources.EventSimResource;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystem;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkload;

/**
 * Starts an EventSim simulation.
 * <p>
 * Connects to a simulation dock (see {@link DockModel}) to display simulation progress.
 * 
 * @author Philipp Merkle
 *
 */
public class StartSimulationJob extends AbstractExtendableJob<MDSDBlackboard> {

	private final SimulationComponentWorkflowConfiguration configuration;

	public StartSimulationJob(SimulationComponentWorkflowConfiguration configuration) {
		this.configuration = configuration;
	}

	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		// derive configuration
		SimulationConfiguration config = configuration.getSimulationConfiguration();
		
		// obtain PCM model from MDSD blackboard
		// TODO multiple repositories are not supported by the following
		PCMResourceSetPartition p = (PCMResourceSetPartition) getBlackboard()
				.getPartition(LoadPCMModelsIntoBlackboardJob.PCM_MODELS_PARTITION_ID);
		PCMModel model = new PCMModel(p.getAllocation(), p.getRepositories().get(0), p.getResourceEnvironment(),
				p.getSystem(), p.getUsageModel(), p.getResourceTypeRepository());
		config.setModel(model);

		// instantiate middleware
		SimulationMiddleware middleware = new SimulationMiddleware(config);

		// instantiate R measurement store
		RMeasurementStore measurementStorage = RMeasurementStore.fromLaunchConfiguration(config.getConfigurationMap());
		if (measurementStorage == null) {
			throw new RuntimeException("R measurement store could not bet constructed from launch configuration.");
		}
	
		// assemble simulation components
		Injector injector = Guice.createInjector(new EventSimWorkload(), new EventSimSystem(), new EventSimResource(),
				new AbstractModule() {
					@Override
					protected void configure() {
						bind(ISimulationMiddleware.class).toInstance(middleware);
						bind(MeasurementStorage.class).toInstance(measurementStorage);
					}
				});
		
		// bootstrap simulation by creating initial simulation events before actually starting simulation
		injector.getInstance(IWorkload.class).generate();

		// start simulation and display simulation progress in a simulation dock (progress viewer) 
		SimulationDockWrapper dock = SimulationDockWrapper.getBestFreeDock();
		dock.start();
		middleware.startSimulation(dock);
		measurementStorage.finish();
		dock.stop();

		super.execute(monitor); // TODO needed?
	}

	

}

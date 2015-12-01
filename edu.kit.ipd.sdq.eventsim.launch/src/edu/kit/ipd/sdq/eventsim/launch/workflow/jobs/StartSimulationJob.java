package edu.kit.ipd.sdq.eventsim.launch.workflow.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.analyzer.workflow.blackboard.PCMResourceSetPartition;
import org.palladiosimulator.analyzer.workflow.jobs.LoadPCMModelsIntoBlackboardJob;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.uka.ipd.sdq.codegen.simucontroller.dockmodel.DockModel;
import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.launch.DefaultSimulationModule;
import edu.kit.ipd.sdq.eventsim.launch.SimulationDockWrapper;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.launch.runconfig.EventSimWorkflowConfiguration;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;

/**
 * Starts an EventSim simulation.
 * <p>
 * Connects to a simulation dock (see {@link DockModel}) to display simulation progress.
 * 
 * @author Philipp Merkle
 *
 */
public class StartSimulationJob extends AbstractExtendableJob<MDSDBlackboard> {

	private final EventSimWorkflowConfiguration configuration;

	public StartSimulationJob(EventSimWorkflowConfiguration configuration) {
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

		// assemble simulation components...
		Injector injector = Guice.createInjector(new DefaultSimulationModule(config));

		// ...and start simulation, displaying simulation progress in a simulation dock (progress viewer)
		SimulationDockWrapper dock = SimulationDockWrapper.getBestFreeDock();
		dock.start();
		injector.getInstance(SimulationManager.class).startSimulation(dock);
		dock.stop();

		super.execute(monitor); // TODO needed?
	}

}

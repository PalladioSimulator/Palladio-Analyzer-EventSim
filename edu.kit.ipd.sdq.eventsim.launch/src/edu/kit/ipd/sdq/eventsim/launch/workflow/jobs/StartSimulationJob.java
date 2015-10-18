package edu.kit.ipd.sdq.eventsim.launch.workflow.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.palladiosimulator.analyzer.workflow.blackboard.PCMResourceSetPartition;
import org.palladiosimulator.analyzer.workflow.jobs.LoadPCMModelsIntoBlackboardJob;

import de.uka.ipd.sdq.codegen.simucontroller.SimuControllerPlugin;
import de.uka.ipd.sdq.codegen.simucontroller.dockmodel.DockModel;
import de.uka.ipd.sdq.simulation.IStatusObserver;
import de.uka.ipd.sdq.workflow.extension.AbstractExtendableJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;
import edu.kit.ipd.sdq.eventsim.launch.Activator;
import edu.kit.ipd.sdq.eventsim.launch.runconfig.SimulationComponentWorkflowConfiguration;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.PCMModel;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.osgi.ISimulationManager;

public class StartSimulationJob extends AbstractExtendableJob<MDSDBlackboard> {

	private SimulationComponentWorkflowConfiguration configuration;

	public StartSimulationJob(SimulationComponentWorkflowConfiguration configuration) {
		this.configuration = configuration;
	}

	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		// obtain PCM model from MDSD blackboard
		PCMResourceSetPartition p = (PCMResourceSetPartition) getBlackboard()
				.getPartition(LoadPCMModelsIntoBlackboardJob.PCM_MODELS_PARTITION_ID);
		PCMModel model = new PCMModel(p.getAllocation(), p.getRepositories().get(0), p.getResourceEnvironment(),
				p.getSystem(), p.getUsageModel(), p.getResourceTypeRepository());

		// derive configuration
		SimulationConfiguration config = configuration.getSimulationConfiguration();
		config.setModel(model);

		// obtain simulation manager service
		BundleContext bundleCtx = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference<ISimulationManager> managerRef = bundleCtx.getServiceReference(ISimulationManager.class);
		ISimulationManager manager = bundleCtx.getService(managerRef);

		// start simulation
		int simulationId = manager.prepareSimulation(config);

		// setup simulation dock (progress viewer)
		DockModel dock = null;
		try {
			dock = SimuControllerPlugin.getDockModel().getBestFreeDock();
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO
		}

		final DockModel activeDock = dock;

		activeDock.setStarted(true); // TODO needed?
		activeDock.setIdle(false);

		manager.getMiddleware(simulationId).startSimulation(new IStatusObserver() {

			@Override
			public void updateStatus(int percentDone, double currentSimTime, long measurementsTaken) {
				activeDock.setMeasurementCount(measurementsTaken);
				activeDock.setPercentDone(percentDone);
				activeDock.setSimTime(currentSimTime);
			}

		});

		super.execute(monitor); // TODO needed?
	}

}

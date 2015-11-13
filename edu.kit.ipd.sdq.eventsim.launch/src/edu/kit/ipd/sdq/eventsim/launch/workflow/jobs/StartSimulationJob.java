package edu.kit.ipd.sdq.eventsim.launch.workflow.jobs;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
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
import edu.kit.ipd.sdq.eventsim.osgi.ISimulationManager;

/**
 * Starts an EventSim simulation.
 * <p>
 * Connects to a simulation dock (see {@link DockModel}) to display simulation progress.
 * 
 * @author Philipp Merkle
 *
 */
public class StartSimulationJob extends AbstractExtendableJob<MDSDBlackboard> {

	private static final String DOCK_IDLE_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/DOCK_IDLE";
	private static final String SIM_STOPPED_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/SIM_STOPPED";
	private static final String SIM_STARTED_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/SIM_STARTED";
	private static final String DOCK_BUSY_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/DOCK_BUSY";

	private SimulationComponentWorkflowConfiguration configuration;

	private EventAdmin eventAdmin;

	public StartSimulationJob(SimulationComponentWorkflowConfiguration configuration) {
		this.configuration = configuration;
		this.eventAdmin = discoverEventAdmin();
	}

	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		// obtain PCM model from MDSD blackboard
		PCMResourceSetPartition p = (PCMResourceSetPartition) getBlackboard()
				.getPartition(LoadPCMModelsIntoBlackboardJob.PCM_MODELS_PARTITION_ID);

		// TODO multiple repositories are not supported by the following
		PCMModel model = new PCMModel(p.getAllocation(), p.getRepositories().get(0), p.getResourceEnvironment(),
				p.getSystem(), p.getUsageModel(), p.getResourceTypeRepository());

		// derive configuration
		configuration.getSimulationConfiguration().setModel(model);

		// obtain simulation manager service
		ISimulationManager manager = discoverSimulationManager();

		// start simulation
		int simulationId = manager.prepareSimulation(configuration.getSimulationConfiguration());

		// setup simulation dock (progress viewer)
		DockModel dock = null;
		try {
			dock = SimuControllerPlugin.getDockModel().getBestFreeDock();
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO
		}

		sendEventToSimulationDock(DOCK_BUSY_TOPIC, dock);
		sendEventToSimulationDock(SIM_STARTED_TOPIC, dock);

		// start simulation and keep simulation dock updated about simulation progress
		final DockModel activeDock = dock;
		manager.getMiddleware(simulationId).startSimulation(new IStatusObserver() {
			@Override
			public void updateStatus(int percentDone, double currentSimTime, long measurementsTaken) {
				activeDock.setMeasurementCount(measurementsTaken);
				activeDock.setPercentDone(percentDone);
				activeDock.setSimTime(currentSimTime);
			}
		});

		sendEventToSimulationDock(SIM_STOPPED_TOPIC, dock);
		sendEventToSimulationDock(DOCK_IDLE_TOPIC, dock);

		// clean up
		manager.disposeSimulation(simulationId);

		super.execute(monitor); // TODO needed?
	}

	private EventAdmin discoverEventAdmin() {
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference<EventAdmin> eventServiceRef = context.getServiceReference(EventAdmin.class);
		ServiceTracker eventService = new ServiceTracker<>(context, eventServiceRef, null);
		eventService.open();
		return (EventAdmin) eventService.getService();
	}

	private ISimulationManager discoverSimulationManager() {
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference<ISimulationManager> managerRef = context.getServiceReference(ISimulationManager.class);
		// TODO better use service tracker here?
		return context.getService(managerRef);
	}

	private void sendEventToSimulationDock(String topic, DockModel dock) {
		Map<String, Object> properties = new Hashtable<>();
		properties.put("DOCK_ID", dock.getID());
		eventAdmin.sendEvent(new Event(topic, properties));
	}

}

package edu.kit.ipd.sdq.eventsim.launch.workflow.jobs;

import org.eclipse.core.runtime.CoreException;

import de.uka.ipd.sdq.codegen.simucontroller.debug.IDebugListener;
import de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs.AbstractSimulationJob;
import edu.kit.ipd.sdq.eventsim.launch.runconfig.SimulationComponentWorkflowConfiguration;

/**
 * The simulation component workflow job. This job generates a nearly empty simulation code project which only delegates
 * the simulation control to the simulation middleware.
 * 
 * This class is based on {@link EventSimJob}. Some code has been reused.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
public class SimulationComponentJob extends AbstractSimulationJob<SimulationComponentWorkflowConfiguration> {

	public SimulationComponentJob(SimulationComponentWorkflowConfiguration configuration, IDebugListener listener,
			boolean loadModels) throws CoreException {
		super(configuration, listener, loadModels);
	}

	public SimulationComponentJob(SimulationComponentWorkflowConfiguration configuration, IDebugListener listener)
			throws CoreException {
		super(configuration, listener);
	}

	public SimulationComponentJob(SimulationComponentWorkflowConfiguration configuration) throws CoreException {
		super(configuration);
	}

	@Override
	protected void addSimulatorSpecificJobs(final SimulationComponentWorkflowConfiguration configuration) {
		// prepare and start simulation
		this.addJob(new StartSimulationJob(configuration));

		// 4. Transfer the JAR to a free simulation dock and simulate it
		// TODO connect dock to simulation service
//		this.addJob(new TransferSimulationBundleToDock(configuration, debugListener, buildBundleJob));
		
		
	}

}

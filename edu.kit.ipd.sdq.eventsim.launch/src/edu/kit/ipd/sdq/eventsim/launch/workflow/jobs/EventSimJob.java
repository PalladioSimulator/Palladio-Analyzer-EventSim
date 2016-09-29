package edu.kit.ipd.sdq.eventsim.launch.workflow.jobs;

import org.eclipse.core.runtime.CoreException;

import de.uka.ipd.sdq.codegen.simucontroller.debug.IDebugListener;
import de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs.AbstractSimulationJob;
import de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs.WorkflowHooks;
import edu.kit.ipd.sdq.eventsim.launch.runconfig.EventSimWorkflowConfiguration;

/**
 * The simulation component workflow job. This job generates a nearly empty simulation code project
 * which only delegates the simulation control to the simulation middleware.
 * 
 * This class is based on {@link EventSimJob}. Some code has been reused.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
public class EventSimJob extends AbstractSimulationJob<EventSimWorkflowConfiguration> {

    public EventSimJob(EventSimWorkflowConfiguration configuration, IDebugListener listener, boolean loadModels)
            throws CoreException {
        super(configuration, listener, loadModels);
    }

    public EventSimJob(EventSimWorkflowConfiguration configuration, IDebugListener listener) throws CoreException {
        super(configuration, listener);
    }

    public EventSimJob(EventSimWorkflowConfiguration configuration) throws CoreException {
        super(configuration);
    }

    @Override
    protected void addSimulatorSpecificJobs(final EventSimWorkflowConfiguration configuration) {
        // All Workflow extension jobs with the extension hook id
        // WORKFLOW_ID_BEFORE_DOCK
        handleJobExtensions(WorkflowHooks.WORKFLOW_ID_BEFORE_DOCK, configuration);

        // prepare and start simulation
        this.addJob(new StartSimulationJob(configuration));

        // All Workflow extension jobs with the extension hook id
        // WORKFLOW_ID_AFTER_SIMULATION
        handleJobExtensions(WorkflowHooks.WORKFLOW_ID_AFTER_SIMULATION, configuration);
    }

    @Override
    public String getName() {
        return "Running EventSim simulation";
    }

}

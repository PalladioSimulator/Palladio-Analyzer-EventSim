package edu.kit.ipd.sdq.eventsim.workload.generator;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.OpenWorkload;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;
import org.palladiosimulator.pcm.usagemodel.Workload;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.ICommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.IPCMCommand;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.UnexpectedModelStructureException;

/**
 * This command creates and returns a list of all {@link WorkloadGenerator}s for a PCM usage model.
 * 
 * @author Philipp Merkle
 * 
 */
public class BuildWorkloadGenerator implements IPCMCommand<List<WorkloadGenerator>> {

    private WorkloadGeneratorFactory factory;

    public BuildWorkloadGenerator(WorkloadGeneratorFactory factory) {
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorkloadGenerator> execute(final PCMModel pcm, final ICommandExecutor<PCMModel> executor) {
        final List<WorkloadGenerator> workloads = new ArrayList<WorkloadGenerator>();
        for (final UsageScenario u : pcm.getUsageModel().getUsageScenario_UsageModel()) {
            final Workload w = u.getWorkload_UsageScenario();
            if (UsagemodelPackage.eINSTANCE.getOpenWorkload().isInstance(w)) {
                OpenWorkloadGenerator generator = factory.createOpen((OpenWorkload) w);
                workloads.add(generator);
            } else if (UsagemodelPackage.eINSTANCE.getClosedWorkload().isInstance(w)) {
                ClosedWorkloadGenerator generator = factory.createClosed((ClosedWorkload) w);
                workloads.add(generator);
            } else {
                throw new UnexpectedModelStructureException(
                        "Found a workload which is neither an OpenWorkload nor a ClosedWorkload.");
            }
        }
        return workloads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cachable() {
        return false;
    }

}

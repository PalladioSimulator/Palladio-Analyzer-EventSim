package edu.kit.ipd.sdq.eventsim.workload;

import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.Loop;

import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.ModelDiagnostics;

@Singleton
public class WorkloadModelDiagnostics extends ModelDiagnostics {

    public WorkloadModelDiagnostics() {
        super(DiagnosticsMode.LOG_WARNING_AND_CONTINUE);
        // TODO configure via configuration injection
    }

    public WorkloadModelDiagnostics(DiagnosticsMode mode) {
        super(mode);
    }

    public void reportMissingLoopingBehaviour(Loop action) {
        handle("Missing looping behaviour", action);
    }

    public void reportMissingBranchTransitions(Branch branch) {
        handle("Missing branch transitions", branch);
    }

}

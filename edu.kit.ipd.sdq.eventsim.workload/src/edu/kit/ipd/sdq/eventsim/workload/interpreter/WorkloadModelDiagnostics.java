package edu.kit.ipd.sdq.eventsim.workload.interpreter;

import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.Loop;

import edu.kit.ipd.sdq.eventsim.interpreter.ModelDiagnostics;

public class WorkloadModelDiagnostics extends ModelDiagnostics {

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

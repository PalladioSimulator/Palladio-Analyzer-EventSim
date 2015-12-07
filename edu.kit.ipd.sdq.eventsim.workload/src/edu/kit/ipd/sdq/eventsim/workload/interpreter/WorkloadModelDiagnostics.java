package edu.kit.ipd.sdq.eventsim.workload.interpreter;

import org.palladiosimulator.pcm.usagemodel.Loop;

import edu.kit.ipd.sdq.eventsim.interpreter.ModelDiagnostics;

public class WorkloadModelDiagnostics extends ModelDiagnostics {

	public WorkloadModelDiagnostics(DiagnosticsMode mode) {
		super(mode);
	}
	
	public void missingLoopingBehaviourIn(Loop action) {
		handle("Missing looping behaviour", action);
	}

}

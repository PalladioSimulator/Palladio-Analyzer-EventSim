package edu.kit.ipd.sdq.eventsim.workload.tests;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class UsageScenarioBuilder {

	private final UsageScenario scenario;
	
	public UsageScenarioBuilder() {
		UsageScenario scenario = UsagemodelFactory.eINSTANCE.createUsageScenario();
		this.scenario = scenario;
	}
	
	public UsageScenarioBuilder closedWorkload(int population, double thinkTime) {
		ClosedWorkload w = UsagemodelFactory.eINSTANCE.createClosedWorkload();
		w.setUsageScenario_Workload(scenario);
		w.setPopulation(population);
	
		PCMRandomVariable tt = CoreFactory.eINSTANCE.createPCMRandomVariable();
		tt.setSpecification(new Double(thinkTime).toString());
		tt.setClosedWorkload_PCMRandomVariable(w);
		return this;
	}
	
	public UsageScenario buildIn(UsageModel um) {
		scenario.setUsageModel_UsageScenario(um);
		return scenario;
	}
	
}

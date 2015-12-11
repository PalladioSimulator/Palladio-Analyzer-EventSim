package edu.kit.ipd.sdq.eventsim.test.util.builder.usage;

import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;

public class UsageBuilder {

	private BuildingContext context;
	
	private UsageModel usage;
	
	public UsageBuilder(BuildingContext context) {
		this.context = context;
		this.usage = UsagemodelFactory.eINSTANCE.createUsageModel();
		context.add(usage);
	}
	
	public UsageModel build() {
		return usage;
	}
	
	public UsageScenarioBuilder scenarioBuilder() {
		return new UsageScenarioBuilder(context);
	}
	
	public ScenarioBehaviourBuilder behaviourBuilder() {
		return new ScenarioBehaviourBuilder(context);
	}
	
}

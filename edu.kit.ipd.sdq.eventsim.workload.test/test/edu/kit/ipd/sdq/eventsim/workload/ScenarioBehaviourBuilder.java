package edu.kit.ipd.sdq.eventsim.workload;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Delay;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class ScenarioBehaviourBuilder {

	private final ScenarioBehaviour behaviour;

	private AbstractUserAction lastAction;

	public ScenarioBehaviourBuilder() {
		behaviour = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
	}

	public ScenarioBehaviourBuilder start() {
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		enchain(start);
		return this;
	}

	public ScenarioBehaviourBuilder stop() {
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		enchain(stop);
		return this;
	}

	public ScenarioBehaviourBuilder delay(double time) {
		Delay delay = UsagemodelFactory.eINSTANCE.createDelay();
		PCMRandomVariable delayTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		delayTime.setSpecification(new Double(time).toString());
		delayTime.setDelay_TimeSpecification(delay);
		enchain(delay);
		return this;
	}

	private void enchain(AbstractUserAction action) {
		action.setScenarioBehaviour_AbstractUserAction(behaviour);
		action.setPredecessor(lastAction);
		lastAction = action;
	}

	public ScenarioBehaviour buildIn(UsageScenario scenario) {
		behaviour.setUsageScenario_SenarioBehaviour(scenario);
		return behaviour;
	}

}

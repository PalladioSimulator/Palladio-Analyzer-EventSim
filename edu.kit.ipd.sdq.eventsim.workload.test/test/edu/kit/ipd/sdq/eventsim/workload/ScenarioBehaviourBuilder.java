package edu.kit.ipd.sdq.eventsim.workload;

import java.util.UUID;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
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

	public ScenarioBehaviourBuilder start(String name) {
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		enchain(start);
		return this;
	}

	public ScenarioBehaviourBuilder start() {
		return start(randomName());
	}

	public ScenarioBehaviourBuilder stop(String name) {
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		stop.setEntityName(name);
		enchain(stop);
		return this;
	}

	public ScenarioBehaviourBuilder stop() {
		return stop(randomName());
	}

	public ScenarioBehaviourBuilder delay(String name, double time) {
		Delay delay = UsagemodelFactory.eINSTANCE.createDelay();
		delay.setEntityName(name);
		PCMRandomVariable delayTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		delayTime.setSpecification(new Double(time).toString());
		delayTime.setDelay_TimeSpecification(delay);
		enchain(delay);
		return this;
	}

	public ScenarioBehaviourBuilder delay(double time) {
		return delay(randomName(), time);
	}

	public ScenarioBehaviourBuilder branch(String name, BranchTransition... transitions) {
		Branch branch = UsagemodelFactory.eINSTANCE.createBranch();
		branch.setEntityName(name);
		for (BranchTransition t : transitions) {
			t.setBranch_BranchTransition(branch);
		}
		enchain(branch);
		return this;
	}

	public ScenarioBehaviourBuilder branch(BranchTransition... transitions) {
		return branch(randomName(), transitions);
	}

	public static BranchTransition transition(double probability, ScenarioBehaviour behaviour) {
		BranchTransition t = UsagemodelFactory.eINSTANCE.createBranchTransition();
		t.setBranchProbability(probability);
		t.setBranchedBehaviour_BranchTransition(behaviour);
		return t;
	}

	public ScenarioBehaviour build() {
		return behaviour;
	}

	public ScenarioBehaviour buildIn(UsageScenario scenario) {
		behaviour.setUsageScenario_SenarioBehaviour(scenario);
		return behaviour;
	}

	private void enchain(AbstractUserAction action) {
		action.setScenarioBehaviour_AbstractUserAction(behaviour);
		action.setPredecessor(lastAction);
		lastAction = action;
	}

	private String randomName() {
		return UUID.randomUUID().toString();
	}

	public static AbstractUserAction actionByName(ScenarioBehaviour b, String actionName) {
		TreeIterator<EObject> it = b.eAllContents();
		while (it.hasNext()) {
			EObject o = it.next();
			if (o instanceof AbstractUserAction) {
				AbstractUserAction a = (AbstractUserAction) o;
				if (actionName.equals(a.getEntityName())) {
					return a;
				}
			}
		}
		throw new RuntimeException("Could not find action named " + actionName);
	}

}

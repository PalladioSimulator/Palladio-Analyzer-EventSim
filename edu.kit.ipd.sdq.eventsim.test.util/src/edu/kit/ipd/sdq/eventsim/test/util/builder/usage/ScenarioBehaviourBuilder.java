package edu.kit.ipd.sdq.eventsim.test.util.builder.usage;

import java.util.UUID;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.Delay;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Loop;
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
		start.setEntityName(name);
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
	
	public ScenarioBehaviourBuilder loop(String name, int iterations, ScenarioBehaviour behaviour) {
		Loop loop = UsagemodelFactory.eINSTANCE.createLoop();
		loop.setEntityName(name);
		PCMRandomVariable loopIterations = CoreFactory.eINSTANCE.createPCMRandomVariable();
		loopIterations.setSpecification(new Integer(iterations).toString());
		loop.setLoopIteration_Loop(loopIterations);
		loop.setBodyBehaviour_Loop(behaviour);
		enchain(loop);
		return this;
	}
	
	public ScenarioBehaviourBuilder loop(int iterations, ScenarioBehaviour behaviour) {
		return loop(randomName(), iterations, behaviour);
	}

	public ScenarioBehaviourBuilder call(String name, OperationSignature signature, OperationProvidedRole role) {
		EntryLevelSystemCall call = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
		call.setEntityName(name);
		call.setOperationSignature__EntryLevelSystemCall(signature);
		call.setProvidedRole_EntryLevelSystemCall(role);
		enchain(call);
		return this;
	}
	
	public ScenarioBehaviourBuilder call(OperationSignature signature, OperationProvidedRole role) {
		return call(randomName(), signature, role);
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

	public static AbstractUserAction actionByName(BranchTransition t, String actionName) {
		return actionByName(t.getBranchedBehaviour_BranchTransition(), actionName);
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

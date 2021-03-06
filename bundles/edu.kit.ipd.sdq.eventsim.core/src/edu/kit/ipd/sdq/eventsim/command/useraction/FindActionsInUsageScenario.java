package edu.kit.ipd.sdq.eventsim.command.useraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.ICommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.IPCMCommand;

/**
 * This command returns all {@link AbstractUserAction}s of a specified type that are contained in the specified
 * {@link UsageScenario}. The search is recursive such that action contained in loops or branches, for instance, are
 * returned as well.
 * 
 * @author Philipp Merkle
 * 
 */
public class FindActionsInUsageScenario<A extends AbstractUserAction> implements IPCMCommand<List<A>> {

	private UsageScenario scenario;

	private Class<A> actionType;

	private boolean recurse;

	/**
	 * Constructs a command that returns all EntryLevelSystemCalls contained in the given usage scenario.
	 * 
	 * @param scenario
	 *            the usage scenario
	 */
	public FindActionsInUsageScenario(UsageScenario scenario, Class<A> actionType, boolean recurse) {
		this.scenario = scenario;
		this.actionType = actionType;
		this.recurse = recurse;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<A> execute(PCMModel pcm, ICommandExecutor<PCMModel> executor) {
		ScenarioBehaviour behaviour = scenario.getScenarioBehaviour_UsageScenario();
		return findActionsByType(behaviour, executor);
	}

	/**
	 * Searches for and returns all system calls that are contained in the specified scenario behaviour
	 */
	@SuppressWarnings("unchecked")
	private List<A> findActionsByType(ScenarioBehaviour behaviour, ICommandExecutor<PCMModel> executor) {
		List<A> actions = new ArrayList<>();

		// find start action
		AbstractUserAction currentAction = executor
				.execute(new FindActionInUsageBehaviour<Start>(behaviour, Start.class));
		while (currentAction != null) {
			if (actionType.isInstance(currentAction)) {
				// cast is safe
				actions.add((A) currentAction);
			}
			if (recurse) {
				if (UsagemodelPackage.eINSTANCE.getBranch().isInstance(currentAction)) {
					actions.addAll(findActionsInBranch((Branch) currentAction, executor));
				} else if (UsagemodelPackage.eINSTANCE.getLoop().isInstance(currentAction)) {
					actions.addAll(findActionsInLoop((Loop) currentAction, executor));
				}
			}
			currentAction = currentAction.getSuccessor();
		}
		return actions;
	}

	/**
	 * Searches for and returns all system calls that are contained in the specified branch.
	 */
	private List<A> findActionsInBranch(Branch branch, ICommandExecutor<PCMModel> executor) {
		List<A> calls = new ArrayList<>();
		for (BranchTransition t : branch.getBranchTransitions_Branch()) {
			ScenarioBehaviour behaviour = t.getBranchedBehaviour_BranchTransition();
			calls.addAll(findActionsByType(behaviour, executor));
		}
		return calls;
	}

	/**
	 * Searches for and returns all system calls that are contained in the specified loop.
	 */
	private List<A> findActionsInLoop(Loop loop, ICommandExecutor<PCMModel> executor) {
		ScenarioBehaviour behaviour = loop.getBodyBehaviour_Loop();
		if (behaviour == null) {
			return Collections.emptyList();
		}
		return findActionsByType(behaviour, executor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cachable() {
		return false;
	}

}

package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;

import com.google.inject.Inject;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.InvalidModelParametersException;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.UnknownSimulationException;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.instructions.TraverseUsageBehaviourInstruction;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

/**
 * Simulates {@link Branch} actions.
 * 
 * @author Philipp Merkle
 * 
 */
public class BranchTraversalStrategy implements ITraversalStrategy<AbstractUserAction, Branch, User, UserState> {

	// TODO revisit this tolerance; it's likely better to "fix" the branching probabilities as done in SimuCom.
	private static final double SUM_OF_BRANCHING_PROBABILITES_TOLERANCE = 0.01;
	
	@Inject
	private IRandomGenerator randomGenerator;
	
	@Inject
	private PCMModelCommandExecutor executor;

	@Override
	public ITraversalInstruction<AbstractUserAction, UserState> traverse(final Branch branch, final User user,
			final UserState state) {
		ScenarioBehaviour behaviour = null;

		// no branch transitions? ignore branch and continue with successor.
		if (branch.getBranchTransitions_Branch().size() == 0) {
		    // TODO
//			WorkloadModelDiagnostics diagnostics = user.getEventSimModel().getUsageInterpreter().getDiagnostics();
//			diagnostics.reportMissingBranchTransitions(branch);
			return new TraverseNextAction<>(branch.getSuccessor());
		}

		// randomly select branch transition according to their individual probability
		double sum = 0;
		final double rand = randomGenerator.random();
		for (final BranchTransition t : branch.getBranchTransitions_Branch()) {
			final double p = t.getBranchProbability();
			if (rand >= sum && rand < sum + p) {
				behaviour = t.getBranchedBehaviour_BranchTransition();
			}
			sum += p;
		}

		// fail if branch probabilities don't sum up to one (approximately, as determined by tolerance)
		if (Math.abs(sum - 1.0) > SUM_OF_BRANCHING_PROBABILITES_TOLERANCE) {
			throw new InvalidModelParametersException(
					String.format("Branching probabilities of branch %s sum up to %f, but must be approximately 1.",
							PCMEntityHelper.toString(branch), sum));
		}

		// fail if no branch transaction has been selected
		if (behaviour == null) {
			throw new UnknownSimulationException(String.format("No branch transition has been entered for branch %s",
					PCMEntityHelper.toString(branch)));
		}

		return new TraverseUsageBehaviourInstruction(executor, behaviour, branch.getSuccessor());
	}

}

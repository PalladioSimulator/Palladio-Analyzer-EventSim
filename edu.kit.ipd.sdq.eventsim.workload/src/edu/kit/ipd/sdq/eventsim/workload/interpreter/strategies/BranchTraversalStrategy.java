package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;

import edu.kit.ipd.sdq.eventsim.AbstractEventSimModel;
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

	private static final Logger log = Logger.getLogger(BranchTraversalStrategy.class);

	private static final double SUM_OF_BRANCHING_PROBABILITES_TOLERANCE = 0.01;

	@Override
	public ITraversalInstruction<AbstractUserAction, UserState> traverse(final Branch branch, final User user,
			final UserState state) {
		AbstractEventSimModel model = user.getEventSimModel();
		ScenarioBehaviour behaviour = null;

		// no branch transitions? ignore branch and continue with successor.
		if (branch.getBranchTransitions_Branch().size() == 0) {
			log.warn(String.format(
					"No branch transitions found for branch %s. Ignoring branch and continuing with successor.",
					PCMEntityHelper.toString(branch)));
			return new TraverseNextAction<>(branch.getSuccessor());
		}

		// randomly select branch transition according to their individual probability
		double sum = 0;
		final double rand = model.getSimulationMiddleware().getRandomGenerator().random();
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

		return new TraverseUsageBehaviourInstruction(model, behaviour, branch.getSuccessor());
	}

}

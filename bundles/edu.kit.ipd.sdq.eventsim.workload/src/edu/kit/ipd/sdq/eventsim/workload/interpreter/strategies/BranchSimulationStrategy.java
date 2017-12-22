package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;

import com.google.inject.Inject;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.InvalidModelParametersException;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.UnknownSimulationException;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;
import edu.kit.ipd.sdq.eventsim.workload.WorkloadModelDiagnostics;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;

/**
 * Simulates {@link Branch} actions.
 * 
 * @author Philipp Merkle
 * 
 */
public class BranchSimulationStrategy implements SimulationStrategy<AbstractUserAction, User> {

    // TODO revisit this tolerance; it's likely better to "fix" the branching probabilities as done
    // in SimuCom.
    private static final double SUM_OF_BRANCHING_PROBABILITES_TOLERANCE = 0.01;

    @Inject
    private IRandomGenerator randomGenerator;

    @Inject
    private WorkloadModelDiagnostics diagnostics;

    @Override
    public void simulate(AbstractUserAction action, User user, Consumer<TraversalInstruction> onFinishCallback) {
        Branch branch = (Branch) action;

        // no branch transitions? report and continue with successor.
        if (branch.getBranchTransitions_Branch().size() == 0) {
            diagnostics.reportMissingBranchTransitions(branch);
            user.simulateAction(branch.getSuccessor());
            return;
        }

        // randomly select branch transition according to their individual probability
        ScenarioBehaviour branchTransition = selectBranchTransition(branch, randomGenerator);

        user.simulateBehaviour(branchTransition, () -> {
            // once branch transition has been simulated
            onFinishCallback.accept(() -> {
                user.simulateAction(branch.getSuccessor());
            });
        });
    }

    private static ScenarioBehaviour selectBranchTransition(Branch branch, IRandomGenerator randomGenerator) {
        ScenarioBehaviour selectedTransition = null;
        double sum = 0;
        final double rand = randomGenerator.random();
        for (final BranchTransition t : branch.getBranchTransitions_Branch()) {
            final double p = t.getBranchProbability();
            if (rand >= sum && rand < sum + p) {
                selectedTransition = t.getBranchedBehaviour_BranchTransition();
            }
            sum += p;
        }

        failIfNoTransitionSelected(branch, selectedTransition);
        failIfProbabilitiesDontSumUpToOne(branch, sum);

        return selectedTransition;
    }

    private static void failIfNoTransitionSelected(Branch branch, ScenarioBehaviour selectedTransition) {
        if (selectedTransition == null) {
            throw new UnknownSimulationException(String.format("No branch transition has been entered for branch %s",
                    PCMEntityHelper.toString(branch)));
        }
    }

    private static void failIfProbabilitiesDontSumUpToOne(Branch branch, double sum) {
        if (Math.abs(sum - 1.0) > SUM_OF_BRANCHING_PROBABILITES_TOLERANCE) {
            throw new InvalidModelParametersException(
                    String.format("Branching probabilities of branch %s sum up to %f, but must be approximately 1.",
                            PCMEntityHelper.toString(branch), sum));
        }
    }

}

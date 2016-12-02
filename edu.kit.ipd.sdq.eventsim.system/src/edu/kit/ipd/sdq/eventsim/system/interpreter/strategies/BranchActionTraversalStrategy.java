package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.List;
import java.util.function.Consumer;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.GuardedBranchTransition;
import org.palladiosimulator.pcm.seff.ProbabilisticBranchTransition;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;

import com.google.inject.Inject;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.UnexpectedModelStructureException;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

/**
 * This traversal strategy is responsible for {@link BranchAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class BranchActionTraversalStrategy implements SimulationStrategy<AbstractAction, Request> {

    @Inject
    private IRandomGenerator randomGenerator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<Procedure> onFinishCallback) {
        BranchAction branchAction = (BranchAction) action;

        // check model
        final List<AbstractBranchTransition> transitions = branchAction.getBranches_Branch();
        if (transitions.isEmpty()) {
            throw new UnexpectedModelStructureException(
                    "No branch transitions could be found for branch " + PCMEntityHelper.toString(branchAction));
        }

        // select branch transition
        ResourceDemandingBehaviour behaviour = selectBranchTransition(branchAction,
                request.getRequestState().getStoExContext(), randomGenerator);

        // simulate branch transition
        request.simulateBehaviour(behaviour, request.getCurrentComponent(), () -> {
            // continue with next action
            onFinishCallback.accept(() -> {
                request.simulateAction(branchAction.getSuccessor_AbstractAction());
            });
        });
    }

    private static ResourceDemandingBehaviour selectBranchTransition(BranchAction branch, StackContext stackContext,
            IRandomGenerator randomGenerator) {
        final AbstractBranchTransition firstTransition = branch.getBranches_Branch().get(0);
        if (firstTransition instanceof ProbabilisticBranchTransition) {
            return selectProbabilisticBranchTransition(branch, randomGenerator);
        } else if (firstTransition instanceof GuardedBranchTransition) {
            return selectGuardedBranchTransition(branch, stackContext);
        } else {
            throw new UnexpectedModelStructureException(
                    "The branch transition is expected to be either a ProbabilisticBranchTransition or a GuardedBranchTransition, but found a "
                            + firstTransition.eClass().getName());
        }

    }

    private static ResourceDemandingBehaviour selectProbabilisticBranchTransition(BranchAction branch,
            IRandomGenerator randomGenerator) {
        ResourceDemandingBehaviour selectedBehaviour = null;

        double sum = 0;
        final double rand = randomGenerator.random();

        boolean enteredTransition = false;
        for (final AbstractBranchTransition t : branch.getBranches_Branch()) {
            final ProbabilisticBranchTransition transition = (ProbabilisticBranchTransition) t;
            assert (sum >= 0 && sum <= 1) : "Expected sum to be in the interval [0, 1], but was " + sum;
            final double p = transition.getBranchProbability();
            if (rand >= sum && rand < sum + p) {
                enteredTransition = true;
                selectedBehaviour = transition.getBranchBehaviour_BranchTransition();
            }
            sum += p;
        }

        // TODO better use exception, or diagnostics message
        assert (enteredTransition) : "No branch transition has been entered.";

        return selectedBehaviour;
    }

    private static ResourceDemandingBehaviour selectGuardedBranchTransition(BranchAction branch,
            StackContext stackContext) {
        ResourceDemandingBehaviour selectedBehaviour = null;

        boolean enteredTransition = false;
        for (final AbstractBranchTransition t : branch.getBranches_Branch()) {
            final GuardedBranchTransition transition = (GuardedBranchTransition) t;
            String conditionSpec = transition.getBranchCondition_GuardedBranchTransition().getSpecification();
            Boolean condition = stackContext.evaluate(conditionSpec, Boolean.class);
            if (condition.booleanValue() == true) {
                enteredTransition = true;
                selectedBehaviour = transition.getBranchBehaviour_BranchTransition();
            }
        }

        // TODO better use exception, or diagnostics message
        assert (enteredTransition) : "No branch transition has been entered.";

        return selectedBehaviour;
    }

}

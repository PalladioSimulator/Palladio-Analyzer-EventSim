package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.interpreter.state.ITraversalStrategyState;
import edu.kit.ipd.sdq.eventsim.interpreter.state.InternalState;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.instructions.TraverseUsageBehaviourInstruction;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

/**
 * This traversal strategy is responsible for {@link Loop} actions.
 * 
 * @author Philipp Merkle
 * 
 */
public class LoopTraversalStrategy extends DecoratingTraversalStrategy<AbstractUserAction, Loop, User, UserState> {

    private static Logger logger = Logger.getLogger(LoopTraversalStrategy.class);

    private static final String ITERATION_CURRENT_PROPERTY = "iterationCurrent";

    private static final String ITERATION_OVERALL_PROPERTY = "iterationOverall";

    @Inject
    private PCMModelCommandExecutor executor;

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractUserAction, UserState> traverse(final Loop loop, final User user,
            final UserState state) {
        traverseDecorated(loop, user, state);

        // restore or create state
        ITraversalStrategyState internalState = state.getInternalState(loop);
        if (internalState == null) {
            internalState = this.initialiseState(user, loop, state);
            state.addInternalState(loop, internalState);
        }
        int iterationCurrent = state.getInternalState(loop).getProperty(ITERATION_CURRENT_PROPERTY, 0);
        int iterationOverall = state.getInternalState(loop).getProperty(ITERATION_OVERALL_PROPERTY, 0);

        if (iterationCurrent <= iterationOverall) { // TODO check in debugger
            if (logger.isDebugEnabled()) {
                logger.debug("Traversing iteration " + iterationCurrent + " of " + iterationOverall);
            }

            // increment iteration counter
            internalState.setProperty(ITERATION_CURRENT_PROPERTY, ++iterationCurrent);

            // traverse the body behaviour
            final ScenarioBehaviour behaviour = loop.getBodyBehaviour_Loop();
            if (behaviour == null) {
                // TODO
                // WorkloadModelDiagnostics diagnostics =
                // user.getEventSimModel().getUsageInterpreter().getDiagnostics();
                // diagnostics.reportMissingLoopingBehaviour(loop);
                return new TraverseNextAction<>(loop.getSuccessor());
            }
            return new TraverseUsageBehaviourInstruction(executor, behaviour, loop);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Completed loop traversal");
            }
            state.removeInternalState(loop);
            return new TraverseNextAction<>(loop.getSuccessor());
        }
    }

    private ITraversalStrategyState initialiseState(final User user, final Loop loop, final UserState state) {
        ITraversalStrategyState internalState = new InternalState(); // TODO move to core

        // evaluate the iteration count
        final PCMRandomVariable loopCountRandVar = loop.getLoopIteration_Loop();
        final Integer overallIterations = StackContext.evaluateStatic(loopCountRandVar.getSpecification(),
                Integer.class);

        // create and set state
        internalState.setProperty(ITERATION_OVERALL_PROPERTY, overallIterations);
        internalState.setProperty(ITERATION_CURRENT_PROPERTY, 1);

        return internalState;
    }

}

package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.LoopAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.interpreter.state.ITraversalStrategyState;
import edu.kit.ipd.sdq.eventsim.interpreter.state.InternalState;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.instructions.TraverseComponentBehaviourInstruction;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

/**
 * This traversal strategy is responsible for {@link LoopAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class LoopActionTraversalStrategy
        extends DecoratingTraversalStrategy<AbstractAction, LoopAction, Request, RequestState> {

    private static Logger logger = Logger.getLogger(LoopActionTraversalStrategy.class);

    private static final String ITERATION_CURRENT_PROPERTY = "iterationCurrent";

    private static final String ITERATION_OVERALL_PROPERTY = "iterationOverall";

    @Inject
    private PCMModelCommandExecutor executor;

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(final LoopAction loop, final Request request,
            final RequestState state) {
        traverseDecorated(loop, request, state);

        // restore or create state
        ITraversalStrategyState internalState = state.getInternalState(loop);
        if (internalState == null) {
            internalState = this.initialiseState(request, loop, state);
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
            final ResourceDemandingBehaviour behaviour = loop.getBodyBehaviour_Loop();
            return new TraverseComponentBehaviourInstruction(executor, behaviour, state.getComponent(), loop);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Completed loop traversal");
            }
            return new TraverseNextAction<>(loop.getSuccessor_AbstractAction());
        }
    }

    private ITraversalStrategyState initialiseState(final Request request, final LoopAction loop,
            final RequestState state) {
        ITraversalStrategyState internalState = new InternalState(); // TODO move to core

        // evaluate the iteration count
        final PCMRandomVariable loopCountRandVar = loop.getIterationCount_LoopAction();
        final Integer overallIterations = state.getStoExContext().evaluate(loopCountRandVar.getSpecification(),
                Integer.class);

        // create and set state
        internalState.setProperty(ITERATION_OVERALL_PROPERTY, overallIterations);
        internalState.setProperty(ITERATION_CURRENT_PROPERTY, 1);

        return internalState;
    }

}

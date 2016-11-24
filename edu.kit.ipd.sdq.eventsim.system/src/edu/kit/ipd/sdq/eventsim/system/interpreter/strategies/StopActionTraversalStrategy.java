package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.StopAction;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.events.SystemRequestFinishedEvent;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.EndTraversal;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseAfterLeavingScope;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

/**
 * This traversal strategy is responsible for {@link StopAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class StopActionTraversalStrategy
        extends DecoratingTraversalStrategy<AbstractAction, StopAction, Request, RequestState> {

    @Inject
    private ISimulationMiddleware middleware;

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(final StopAction stop, final Request request,
            final RequestState state) {
        traverseDecorated(stop, request, state);
        if (state.hasOpenScope()) {
            return new TraverseAfterLeavingScope<>();
        } else {
            if (state.isForkedRequestState()) {
                return new EndTraversal<>();
            } else {

                // fire seff traversal completed event
                middleware.triggerEvent(new SystemRequestFinishedEvent(request));

                return new EndTraversal<>();
            }

        }
    }

}

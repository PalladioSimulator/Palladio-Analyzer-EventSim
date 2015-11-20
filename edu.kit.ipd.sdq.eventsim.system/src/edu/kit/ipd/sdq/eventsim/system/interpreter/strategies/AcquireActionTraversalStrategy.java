package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AcquireAction;

import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.components.UnboundRequiredRoleAccessException;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.InterruptTraversal;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystemModel;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.events.ResumeSeffTraversalEvent;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

/**
 * This traversal strategy is responsible for {@link AcquireAction}s.
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 * 
 */
public class AcquireActionTraversalStrategy implements ITraversalStrategy<AbstractAction, AcquireAction, Request, RequestState> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(final AcquireAction action, final Request request, final RequestState state) {
        if (!action.getResourceDemand_Action().isEmpty()) {
            throw new EventSimException("Parametric resource demands are not yet supported for AcquireActions.");
        }

		// store EventSim specific state to the request
        request.setRequestState(state); // TODO why setting this here?
        
        final PassiveResource passiveResouce = action.getPassiveresource_AcquireAction();
        AssemblyContext ctx = state.getComponent().getAssemblyCtx();
		boolean acquired = request.getEventSimModel().getComponent().getRequiredService(IPassiveResource.class)
				.acquire(request, ctx, passiveResouce, 1);
        	
        // TODO warning if timeout is set to true in model
        
        if (acquired) {
        	return new TraverseNextAction<>(action.getSuccessor_AbstractAction());
        } else {
        	EventSimSystemModel model = (EventSimSystemModel) request.getEventSimModel();
            request.passivate(new ResumeSeffTraversalEvent(model, state));

            // here, it is assumed that the passive resource grants access to waiting processes as
            // soon as the requested capacity becomes available. Thus, we do not need to acquire the
            // passive resource again as this will be done within the release method. Accordingly
            // the traversal resumes with the successor of this action.
            return new InterruptTraversal<>(action.getSuccessor_AbstractAction());
        }
    }

}

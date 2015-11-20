package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ReleaseAction;

import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

/**
 * This traversal strategy is responsible for {@link ReleaseAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class ReleaseActionTraversalStrategy
		implements ITraversalStrategy<AbstractAction, ReleaseAction, Request, RequestState> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITraversalInstruction<AbstractAction, RequestState> traverse(ReleaseAction action, Request request,
			RequestState state) {
		if (!action.getResourceDemand_Action().isEmpty()) {
			throw new EventSimException("Parametric resource demands are not yet supported for ReleaseActions.");
		}

		// store EventSim specific state to the request
		request.setRequestState(state);

		final PassiveResource passiveResouce = action.getPassiveResource_ReleaseAction();
		AssemblyContext ctx = state.getComponent().getAssemblyCtx();
		
		request.getEventSimModel().getComponent().getRequiredService(IPassiveResource.class).release(request, ctx,
				passiveResouce, 1);

		return new TraverseNextAction<>(action.getSuccessor_AbstractAction());
	}

}

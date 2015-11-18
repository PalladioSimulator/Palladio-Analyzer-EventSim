package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import edu.kit.ipd.sdq.eventsim.api.events.EntryLevelSystemCallEvent;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.InterruptTraversal;
import edu.kit.ipd.sdq.eventsim.middleware.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

/**
 * This traversal strategy is responsible to create service calls on a system
 * simulation component based on {@link EntryLevelSystemCall} actions.
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 */
public class EntryLevelSystemCallTraversalStrategy implements ITraversalStrategy<AbstractUserAction, EntryLevelSystemCall, User, UserState> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITraversalInstruction<AbstractUserAction, UserState> traverse(final EntryLevelSystemCall call, final User user, final UserState state) {
		// store EventSim specific state to the user
		user.setUserState(state); // TODO redundant!?

		// invoke system service
		ISimulationMiddleware middleware = user.getEventSimModel().getSimulationMiddleware();
		middleware.triggerEvent(new EntryLevelSystemCallEvent(user, call)); 

		// interrupt the usage traversal until service call simulation finished
		return new InterruptTraversal<>(call.getSuccessor());
	}

}

package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;

/**
 * This traversal strategy is responsible to create service calls on a system simulation component
 * based on {@link EntryLevelSystemCall} actions.
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 */
public class EntryLevelSystemCallSimulationStrategy implements SimulationStrategy<AbstractUserAction, User> {

    @Inject
    private ISystem system;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractUserAction action, User user, Consumer<TraversalInstruction> onFinishCallback) {
        EntryLevelSystemCall call = (EntryLevelSystemCall) action;

        // 1) invoke system-provided service
        system.callService(user, call, () -> {
            // 2) when the service call finishes, return traversal instruction
            onFinishCallback.accept(() -> {
                // 3) once called, continue simulation with successor
                user.simulateAction(call.getSuccessor());
            });
        });
    }

}

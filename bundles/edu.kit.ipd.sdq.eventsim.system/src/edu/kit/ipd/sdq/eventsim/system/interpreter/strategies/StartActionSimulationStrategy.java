package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.StartAction;

import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

/**
 * This traversal strategy is responsible for {@link StartAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class StartActionSimulationStrategy implements SimulationStrategy<AbstractAction, Request> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<TraversalInstruction> onFinishCallback) {
        // 1) return traversal instruction
        onFinishCallback.accept(() -> {
            // 2) once called, continue simulation with successor
            request.simulateAction(action.getSuccessor_AbstractAction());
        });
    }

}

package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.LoopAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.interpreter.LoopIterationHandler;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

/**
 * This traversal strategy is responsible for {@link LoopAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class LoopActionTraversalStrategy implements SimulationStrategy<AbstractAction, Request> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<Procedure> onFinishCallback) {
        LoopAction loop = (LoopAction) action;

        // report model issues, if any
        final ResourceDemandingBehaviour behaviour = loop.getBodyBehaviour_Loop();
        if (behaviour == null) {
            // TODO
            // diagnostics.reportMissingLoopingBehaviour(loop);
        }

        // evaluate iteration count
        final PCMRandomVariable loopCountRandVar = loop.getIterationCount_LoopAction();
        final int requestedIterations = StackContext.evaluateStatic(loopCountRandVar.getSpecification(), Integer.class);

        // 1) simulate loop iterations
        new LoopIterationHandler(requestedIterations, self -> {
            // 2) for each iteration (except the last iteration)
            request.simulateBehaviour(behaviour, request.getCurrentComponent(), self);
        }, self -> {
            // 3) only for the last iteration
            request.simulateBehaviour(behaviour, request.getCurrentComponent(), () -> {
                // when last iteration completes...
                onFinishCallback.accept(() -> {
                    // 4) continue with loop successor
                    request.simulateAction(loop.getSuccessor_AbstractAction());
                });
            });
        }).execute(); // bootstrap first iteration
    }

}

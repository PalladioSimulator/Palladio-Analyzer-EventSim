package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import edu.kit.ipd.sdq.eventsim.interpreter.LoopIterationHandler;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.WorkloadModelDiagnostics;

/**
 * This traversal strategy is responsible for {@link Loop} actions.
 * 
 * @author Philipp Merkle
 * 
 */
public class LoopTraversalStrategy implements SimulationStrategy<AbstractUserAction, User> {

    @Inject
    private WorkloadModelDiagnostics diagnostics;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractUserAction action, User user, Consumer<TraversalInstruction> onFinishCallback) {
        // TODO simulate decorated

        Loop loop = (Loop) action;

        // report model issues, if any
        final ScenarioBehaviour behaviour = loop.getBodyBehaviour_Loop();
        if (behaviour == null) {
            diagnostics.reportMissingLoopingBehaviour(loop);
            user.simulateAction(loop.getSuccessor());
            return;
        }

        // evaluate iteration count
        final PCMRandomVariable loopCountRandVar = loop.getLoopIteration_Loop();
        final int requestedIterations = StackContext.evaluateStatic(loopCountRandVar.getSpecification(), Integer.class);

        // 1) simulate loop iterations
        new LoopIterationHandler(requestedIterations, self -> {
            // 2) for each iteration (except the last iteration)
            user.simulateBehaviour(behaviour, self);
        }, self -> {
            // 3) only for the last iteration
            user.simulateBehaviour(behaviour, () -> {
                // 4) when last iteration completes, return traversal instruction
                onFinishCallback.accept(() -> {
                    // 5) once called, continue simulation with successor
                    user.simulateAction(loop.getSuccessor());
                });

            });
        }).execute(); // bootstrap first iteration
    }

}

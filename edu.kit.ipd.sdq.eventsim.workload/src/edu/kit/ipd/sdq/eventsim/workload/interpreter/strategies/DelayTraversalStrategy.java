package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Delay;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;

/**
 * This traversal strategy is responsible for {@link Delay} actions.
 * 
 * @author Philipp Merkle
 * 
 */
public class DelayTraversalStrategy implements SimulationStrategy<AbstractUserAction, User> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractUserAction action, User user, Consumer<Procedure> onFinishCallback) {
        Delay delay = (Delay) action;

        // evaluate StoEx
        final PCMRandomVariable delayTimeSpecification = delay.getTimeSpecification_Delay();
        final double delayTime = NumberConverter
                .toDouble(StackContext.evaluateStatic(delayTimeSpecification.getSpecification()));

        // 1) wait desired time
        user.delay(delayTime, () -> {
            // 2) when waiting time elapsed, return traversal instruction   
            onFinishCallback.accept(() -> {
                // 3) once called, continue simulation with successor
                user.simulateAction(delay.getSuccessor());
            });
        });
    }

}

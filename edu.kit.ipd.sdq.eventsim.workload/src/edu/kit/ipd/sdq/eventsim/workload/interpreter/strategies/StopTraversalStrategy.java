package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Stop;

import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;

/**
 * This traversal strategy is responsible for {@link Stop} actions.
 * 
 * @author Philipp Merkle
 *
 */
public class StopTraversalStrategy implements SimulationStrategy<AbstractUserAction, User> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractUserAction action, User user, Consumer<Procedure> onFinishCallback) {
        // 1) return traversal instruction
        onFinishCallback.accept(() -> {
            // 2) once called, leave the scenario behaviour, which will trigger another callback
            user.leaveScenarioBehaviour();
        });
    }

}

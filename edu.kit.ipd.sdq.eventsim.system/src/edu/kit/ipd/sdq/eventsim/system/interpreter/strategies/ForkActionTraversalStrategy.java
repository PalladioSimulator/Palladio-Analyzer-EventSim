package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.List;
import java.util.function.Consumer;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ForkAction;
import org.palladiosimulator.pcm.seff.ForkedBehaviour;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.system.entities.ForkedRequest;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.entities.RequestFactory;

public class ForkActionTraversalStrategy implements SimulationStrategy<AbstractAction, Request> {

    @Inject
    private RequestFactory requestFactory;

    @Override
    public void simulate(AbstractAction action, Request request, Consumer<Procedure> onFinishCallback) {
        ForkAction fork = (ForkAction) action;

        // TODO support synchronous forks
        if (fork.getSynchronisingBehaviours_ForkAction() != null) {
            throw new EventSimException("Synchronous forked behaviours are not yet supported.");
        }

        List<ForkedBehaviour> asynchronousBehaviours = fork.getAsynchronousForkedBehaviours_ForkAction();
        for (ForkedBehaviour b : asynchronousBehaviours) {
            ForkedRequest forkedRequest = requestFactory.createForkedRequest(b, true, request);
            forkedRequest.simulateBehaviour(b, request.getCurrentComponent(), () -> {
                // nothing to do on completion of asynchronous forked behaviour
            });
        }

        // 1) dont't wait for forked behaviours to finish, return traversal instruction right away
        onFinishCallback.accept(() -> {
            // 2) once called, continue simulation with successor
            request.simulateAction(fork.getSuccessor_AbstractAction());
        });
    }

}

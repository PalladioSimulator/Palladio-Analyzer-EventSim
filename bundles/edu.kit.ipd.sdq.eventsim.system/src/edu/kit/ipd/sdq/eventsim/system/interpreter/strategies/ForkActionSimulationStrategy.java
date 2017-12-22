package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ForkAction;
import org.palladiosimulator.pcm.seff.ForkedBehaviour;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.ForkedRequest;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.entities.RequestFactory;

/**
 * This traversal strategy is responsible for {@link ForkAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class ForkActionSimulationStrategy implements SimulationStrategy<AbstractAction, Request> {

    private static final Logger logger = Logger.getLogger(ForkActionSimulationStrategy.class);

    @Inject
    private RequestFactory requestFactory;

    @Override
    public void simulate(AbstractAction action, Request request, Consumer<TraversalInstruction> onFinishCallback) {
        ForkAction fork = (ForkAction) action;

        List<ForkedBehaviour> asynchronousBehaviours = fork.getAsynchronousForkedBehaviours_ForkAction();
        for (ForkedBehaviour b : asynchronousBehaviours) {
            ForkedRequest forkedRequest = requestFactory.createForkedRequest(b, true, request);
            // simulate asynchronous forks right away, dont't wait for them to finish
            forkedRequest.simulateBehaviour(b, request.getCurrentComponent(), () -> {
                // nothing to do on completion of asynchronous forked behaviour
            });
        }

        // TODO consider getOutputParameterUsage?
        if (!fork.getSynchronisingBehaviours_ForkAction().getOutputParameterUsage_SynchronisationPoint().isEmpty()) {
            logger.warn("Encountered synchronization point with non-empty output parameter usage, "
                    + "which is currently not supporzed by EventSim, and hence will be ignored.");
        }

        List<ForkedBehaviour> synchronousBehaviours = fork.getSynchronisingBehaviours_ForkAction()
                .getSynchronousForkedBehaviours_SynchronisationPoint();
        CountDownBarrier barrier = new CountDownBarrier(synchronousBehaviours.size(), () -> {
            // 1) once every forked behaviour reached the barrier, return traversal strategy
            onFinishCallback.accept(() -> {
                // 2) once called, continue simulation with successor
                request.simulateAction(fork.getSuccessor_AbstractAction());
            });
        });
        for (ForkedBehaviour b : synchronousBehaviours) {
            ForkedRequest forkedRequest = requestFactory.createForkedRequest(b, false, request);
            forkedRequest.simulateBehaviour(b, request.getCurrentComponent(), () -> {
                // count down barrier to indicate this forked behaviour reached the barrier
                barrier.countDown();
            });
        }
    }

    private static class CountDownBarrier {

        private int count;

        private Procedure onZeroCallback;

        /**
         * @param count
         *            the initial count
         * @param onZeroCallback
         *            the callback to be invoked once the count reaches zero
         */
        public CountDownBarrier(int count, Procedure onZeroCallback) {
            this.count = count;
            this.onZeroCallback = onZeroCallback;
            if (count == 0) {
                onZeroCallback.execute();
            }
        }

        /**
         * Decrements the current count by one, starting with the initial count.
         */
        public void countDown() {
            count--;
            if (count == 0) {
                onZeroCallback.execute();
            }
        }

    }

}

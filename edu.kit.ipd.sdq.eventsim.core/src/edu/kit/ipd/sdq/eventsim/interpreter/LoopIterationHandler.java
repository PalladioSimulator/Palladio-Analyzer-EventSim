package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.api.Procedure;

public class LoopIterationHandler implements Procedure {

    private static Logger logger = Logger.getLogger(LoopIterationHandler.class);

    private int currentIteration;

    private final int requestedIterations;

    private final Consumer<LoopIterationHandler> lastIterationCallback;

    private final Consumer<LoopIterationHandler> iterationCallback;

    public LoopIterationHandler(int requestedIterations, Consumer<LoopIterationHandler> iterationCallback,
            Consumer<LoopIterationHandler> lastIterationCallback) {
        this.requestedIterations = requestedIterations;
        this.iterationCallback = iterationCallback;
        this.lastIterationCallback = lastIterationCallback;
    }

    @Override
    public void execute() {
        currentIteration++;
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Simulating loop iteration %s", currentIteration));
        }
        if (currentIteration < requestedIterations) {
            // continue with next iteration
            iterationCallback.accept(this);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("This is the last loop iteration.");
            }
            // finish loop simulation after next iteration
            lastIterationCallback.accept(this);
        }
    }

}

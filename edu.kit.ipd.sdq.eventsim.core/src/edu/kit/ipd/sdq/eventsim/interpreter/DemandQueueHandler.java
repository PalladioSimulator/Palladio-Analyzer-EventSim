package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import edu.kit.ipd.sdq.eventsim.api.Procedure;

public class DemandQueueHandler<T> implements Procedure {

    private final Queue<T> pendingDemands;

    private final BiConsumer<T, DemandQueueHandler<T>> demandHandler;

    private final Consumer<DemandQueueHandler<T>> onCompletionHandler;

    /**
     * @param pendingDemands
     *            the queue of pending demands to be processed
     * @param demandHandler
     *            the behavior to be executed for each individual demand in the queue
     * @param onCompletionHandler
     *            the behavior to be executed once all demands in the queue have been processed
     */
    public DemandQueueHandler(Queue<T> pendingDemands, BiConsumer<T, DemandQueueHandler<T>> demandHandler,
            Consumer<DemandQueueHandler<T>> onCompletionHandler) {
        this.pendingDemands = pendingDemands;
        this.demandHandler = demandHandler;
        this.onCompletionHandler = onCompletionHandler;
    }

    @Override
    public void execute() {
        if (!pendingDemands.isEmpty()) {
            demandHandler.accept(pendingDemands.poll(), this);
        } else {
            onCompletionHandler.accept(this);
        }
    }

}
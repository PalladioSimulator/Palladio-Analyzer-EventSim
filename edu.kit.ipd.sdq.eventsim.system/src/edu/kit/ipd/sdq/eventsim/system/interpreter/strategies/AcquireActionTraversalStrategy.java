package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AcquireAction;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

/**
 * This traversal strategy is responsible for {@link AcquireAction}s.
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 * 
 */
public class AcquireActionTraversalStrategy implements SimulationStrategy<AbstractAction, Request> {

    @Inject
    private IPassiveResource passiveResourceModule;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<TraversalInstruction> onFinishCallback) {
        AcquireAction acquireAction = (AcquireAction) action;

        // TODO warning if timeout is set to true in model

        // check for unsupported feature
        if (!acquireAction.getResourceDemand_Action().isEmpty()) {
            throw new EventSimException("Parametric resource demands are not yet supported for AcquireActions.");
        }

        final PassiveResource passiveResouce = acquireAction.getPassiveresource_AcquireAction();
        AssemblyContext ctx = request.getCurrentComponent().getAssemblyCtx();

        // 1) acquire passive resource
        passiveResourceModule.acquire(request, ctx, passiveResouce, 1, () -> {
            // 2) when granted, return traversal instruction
            onFinishCallback.accept(() -> {
                // 3) once called, continue simulation with successor
                request.simulateAction(acquireAction.getSuccessor_AbstractAction());
            });
        });
    }

}
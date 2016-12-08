package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ReleaseAction;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

/**
 * This traversal strategy is responsible for {@link ReleaseAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class ReleaseActionTraversalStrategy implements SimulationStrategy<AbstractAction, Request> {

    @Inject
    private IPassiveResource passiveResourceModule;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<TraversalInstruction> onFinishCallback) {
        ReleaseAction releaseAction = (ReleaseAction) action;

        if (!releaseAction.getResourceDemand_Action().isEmpty()) {
            throw new EventSimException("Parametric resource demands are not yet supported for ReleaseActions.");
        }

        final PassiveResource passiveResouce = releaseAction.getPassiveResource_ReleaseAction();
        AssemblyContext ctx = request.getCurrentComponent().getAssemblyCtx();

        passiveResourceModule.release(request, ctx, passiveResouce, 1);

        onFinishCallback.accept(() -> {
            request.simulateAction(action.getSuccessor_AbstractAction());
        });
    }

}

package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.instructions.TraverseComponentBehaviourInstruction;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;

/**
 * This traversal strategy is responsible for {@link ExternalCallAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class ExternalCallActionStrategy
        extends DecoratingTraversalStrategy<AbstractAction, ExternalCallAction, Request, RequestState> {

    @Inject
    private PCMModelCommandExecutor executor;

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(final ExternalCallAction action,
            final Request request, final RequestState state) {
        traverseDecorated(action, request, state);

        // find the component which provides the required call
        final ComponentInstance currentComponent = state.getComponent();
        final ComponentInstance providingComponent = currentComponent
                .getProvidingComponent(action.getCalledService_ExternalService());
        final ResourceDemandingSEFF seff = providingComponent
                .getServiceEffectSpecification(action.getCalledService_ExternalService());

        return new TraverseComponentBehaviourInstruction(executor, seff, providingComponent,
                action.getSuccessor_AbstractAction());
    }

}

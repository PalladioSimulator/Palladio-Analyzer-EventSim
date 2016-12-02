package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;

import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;

/**
 * This traversal strategy is responsible for {@link ExternalCallAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class ExternalCallActionStrategy implements SimulationStrategy<AbstractAction, Request> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<Procedure> onFinishCallback) {
        ExternalCallAction callAction = (ExternalCallAction) action;

        // find the component that provides the required service
        final ComponentInstance currentComponent = request.getCurrentComponent();
        final ComponentInstance providingComponent = currentComponent
                .getProvidingComponent(callAction.getCalledService_ExternalService());
        final ResourceDemandingBehaviour behaviour = providingComponent
                .getServiceEffectSpecification(callAction.getCalledService_ExternalService());

        // TODO simulate network, if the component is deployed on another server

        request.simulateBehaviour(behaviour, providingComponent, () -> {
            onFinishCallback.accept(() -> {
                request.simulateAction(action.getSuccessor_AbstractAction());
            });
        });
    }

}

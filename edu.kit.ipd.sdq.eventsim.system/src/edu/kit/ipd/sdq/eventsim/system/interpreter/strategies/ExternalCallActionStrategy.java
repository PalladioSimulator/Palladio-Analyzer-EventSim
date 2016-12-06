package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.ILinkingResource;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.CommunicationLink;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.SimulatedResourceContainer;

/**
 * This traversal strategy is responsible for {@link ExternalCallAction}s.
 * 
 * @author Philipp Merkle
 * 
 */
public class ExternalCallActionStrategy implements SimulationStrategy<AbstractAction, Request> {

    @Inject
    private ILinkingResource network;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<Procedure> onFinishCallback) {
        ExternalCallAction callAction = (ExternalCallAction) action;

        final ComponentInstance currentComponent = request.getCurrentComponent();

        // find the component that provides the required service
        final ComponentInstance providingComponent = currentComponent
                .getProvidingComponent(callAction.getCalledService_ExternalService());
        final ResourceDemandingBehaviour behaviour = providingComponent
                .getServiceEffectSpecification(callAction.getCalledService_ExternalService());

        // is network call?
        SimulatedResourceContainer fromContainer = currentComponent.getResourceContainer();
        SimulatedResourceContainer toContainer = providingComponent.getResourceContainer();
        boolean isNetworkCall = false;
        if (!fromContainer.equals(toContainer)) {
            isNetworkCall = true;
        }

        if (isNetworkCall) {
            CommunicationLink link = fromContainer.findCommunicationLink(toContainer);
            LinkingResource resource = link.getSpecification()
                    .getLinkingResource_CommunicationLinkResourceSpecification();

            double demand = 0; // latency will still be simulated
            // 1) simulate network demand
            network.consume(request, resource, demand, () -> {
                // 2) then simulate component-external call
                request.simulateBehaviour(behaviour, providingComponent, () -> {
                    // 3) when the service call finishes, return traversal instruction
                    onFinishCallback.accept(() -> {
                        // 4) once called, first simulate network demand of result
                        network.consume(request, resource, demand, () -> {
                            // 5) when completed, continue simulation with successor
                            request.simulateAction(action.getSuccessor_AbstractAction());
                        });
                    });
                });
            });
        } else {
            request.simulateBehaviour(behaviour, providingComponent, () -> {
                onFinishCallback.accept(() -> {
                    request.simulateAction(action.getSuccessor_AbstractAction());
                });
            });
        }
    }

}

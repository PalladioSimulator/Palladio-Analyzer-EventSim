package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;
import edu.kit.ipd.sdq.eventsim.api.ILinkingResource;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
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
public class ExternalCallSimulationStrategy implements SimulationStrategy<AbstractAction, Request> {

    private static final Logger logger = Logger.getLogger(ExternalCallSimulationStrategy.class);

    @Inject
    private ILinkingResource network;

    @Inject
    private ISimulationConfiguration configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<TraversalInstruction> onFinishCallback) {
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

            // 1) simulate network demand
            network.consume(request, resource, calculateDemand(request), () -> {
                // 2) then simulate component-external call
                request.simulateBehaviour(behaviour, providingComponent, () -> {
                    // 3) when the service call finishes, return traversal instruction
                    onFinishCallback.accept(() -> {
                        // 4) once called, first simulate network demand of result
                        network.consume(request, resource, calculateDemand(request), () -> {
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

    /*
     * this code is largely taken from SimuCom's model 2 code transformation
     */
    private double calculateDemand(Request request) {
        double demand = 0;
        if (configuration.isSimulateThroughputOfLinkingResources()) {
            // if no stream.BYTESIZE variable is available, the demand is calculated by summing up
            // all the sent variables with BYTESIZE characterization
            List<Entry<String, Object>> stackFrameContent = request.getRequestState().getStoExContext().getStack()
                    .currentStackFrame().getContents();
            for (Entry<String, Object> entry : stackFrameContent) {
                if (entry.getKey().endsWith("BYTESIZE")) {
                    if (entry.getKey().contains(".INNER.")) {
                        // TODO: include logic to determine proper BYTESIZE of the call, take from
                        // completions code.
                        logger.warn(
                                "Network demand cannot be properly determined for INNER BYTESIZE characterizations yet, "
                                        + "the simulation will assume that there is just a single element in the collection. "
                                        + "Please enable the ''simulate middleware marshalling / demarshalling of remote calls'' "
                                        + "in the feature settings tab or directly define the BYTESIZE of the collection.");
                    }
                    demand += NumberConverter.toDouble(entry.getValue());
                }
            }
        } // else the demand stays 0.0; latency will still be simulated
        return demand;
    }

}

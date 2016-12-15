package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.resourcetype.ResourceInterface;
import org.palladiosimulator.pcm.resourcetype.ResourceSignature;
import org.palladiosimulator.pcm.resourcetype.ResourceType;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;
import org.palladiosimulator.pcm.seff.seff_performance.ResourceCall;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

/**
 * This traversal strategy is responsible for {@link InternalAction}s.
 * 
 * TODO remove duplicated code
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 * @author Thomas Zwickl
 * 
 */
public class InternalActionSimulationStrategy implements SimulationStrategy<AbstractAction, Request> {

    private static final Logger logger = Logger.getLogger(InternalActionSimulationStrategy.class);

    @Inject
    private IActiveResource activeResourceModule;

    /**
     * {@inheritDoc}
     */
    @Override
    public void simulate(AbstractAction action, Request request, Consumer<TraversalInstruction> onFinishCallback) {
        InternalAction internalAction = (InternalAction) action;

        // prepare pending resource demands
        Queue<ParametricResourceDemand> pendingDemands = new LinkedList<>();
        for (final ParametricResourceDemand d : internalAction.getResourceDemand_Action()) {
            pendingDemands.add(d);
        }

        // prepare pending resource calls
        Queue<ResourceCall> pendingResourceCalls = new LinkedList<>();
        for (final ResourceCall r : internalAction.getResourceCall__Action()) {
            // ignore all resource calls that go to non-processing resource types
            ResourceType type = findResourceType(r);
            if (isProcessingResourceType(type)) {
                pendingResourceCalls.add(r);
            }
        }

        // 4) simulate resource calls (after resource demand simulation, see below)
        ResourceCallHandler resourceCallHandler = new ResourceCallHandler(pendingResourceCalls,
                (resourceCall, self) -> {
                    // 5) for each resource call
                    simulateProcessingResourceCall(request, resourceCall, self);
                }, self -> {
                    // 6) once all resource calls have been served
                    onFinishCallback.accept(() -> {
                        // 7) continue with next action
                        request.simulateAction(action.getSuccessor_AbstractAction());
                    });
                }); // don't execute right away: will be started once resource resource demands (see
                    // below) have been served

        // 1) simulate resource demands
        new ResourceDemandHandler(pendingDemands, (demand, self) -> {
            // 2) for each resource demand
            simulateResourceDemand(request, demand, self);
        }, self -> {
            // 3) once all resource demands have been served
            resourceCallHandler.execute(); // hand over to resource call handler
        }).execute();

    }

    private void simulateResourceDemand(Request request, ParametricResourceDemand demand, Procedure onServedCallback) {
        if (demand == null) {
            AbstractAction currentAction = request.getRequestState().getCurrentPosition();
            logger.warn("Missing resource demand for " + PCMEntityHelper.toString(currentAction));
            return;
        }

        double evaluatedDemand = NumberConverter.toDouble(request.getRequestState().getStoExContext()
                .evaluate(demand.getSpecification_ParametericResourceDemand().getSpecification()));
        ResourceType type = demand.getRequiredResource_ParametricResourceDemand();
        ResourceContainer resourceContainer = request.getCurrentComponent().getResourceContainer().getSpecification();
        int resourceServiceId = 1;

        // consume the resource demand
        this.activeResourceModule.consume(request, resourceContainer, type, evaluatedDemand, resourceServiceId,
                onServedCallback);
    }

    private void simulateProcessingResourceCall(Request request, ResourceCall resourceCall,
            Procedure onServedCallback) {
        if (resourceCall == null) {
            AbstractAction currentAction = request.getRequestState().getCurrentPosition();
            logger.warn("Missing resource call for " + PCMEntityHelper.toString(currentAction));
            return;
        }

        double evaluatedDemand = NumberConverter.toDouble(request.getRequestState().getStoExContext()
                .evaluate(resourceCall.getNumberOfCalls__ResourceCall().getSpecification()));

        // find the corresponding resource type to be invoked by the resource call
        ResourceType type = findResourceType(resourceCall);

        // find and adjust service id of interface
        ResourceSignature resourceSignature = resourceCall.getSignature__ResourceCall();
        int resourceServiceId = resourceSignature.getResourceServiceId();

        ResourceContainer resourceContainer = request.getCurrentComponent().getResourceContainer().getSpecification();

        // consume the resource demand
        this.activeResourceModule.consume(request, resourceContainer, type, evaluatedDemand, resourceServiceId,
                onServedCallback);
    }

    private ResourceType findResourceType(ResourceCall resourceCall) {
        ResourceInterface resourceInterface = resourceCall.getSignature__ResourceCall()
                .getResourceInterface__ResourceSignature();
        return activeResourceModule.findResourceType(resourceInterface);
    }

    private boolean isProcessingResourceType(ResourceType type) {
        // TODO get rid of instanceof check, if possible
        return type instanceof ProcessingResourceType;
    }

    private static class ResourceDemandHandler implements Procedure {

        private final Queue<ParametricResourceDemand> pendingDemands;

        private final BiConsumer<ParametricResourceDemand, ResourceDemandHandler> demandHandler;

        private final Consumer<ResourceDemandHandler> onCompletionHandler;

        public ResourceDemandHandler(Queue<ParametricResourceDemand> pendingDemands,
                BiConsumer<ParametricResourceDemand, ResourceDemandHandler> demandHandler,
                Consumer<ResourceDemandHandler> onCompletionHandler) {
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

    private static class ResourceCallHandler implements Procedure {

        private Queue<ResourceCall> pendingResourceCalls;

        private final BiConsumer<ResourceCall, ResourceCallHandler> demandHandler;

        private final Consumer<ResourceCallHandler> onCompletionHandler;

        public ResourceCallHandler(Queue<ResourceCall> pendingResourceCalls,
                BiConsumer<ResourceCall, ResourceCallHandler> demandHandler,
                Consumer<ResourceCallHandler> onCompletionHandler) {
            this.pendingResourceCalls = pendingResourceCalls;
            this.demandHandler = demandHandler;
            this.onCompletionHandler = onCompletionHandler;
        }

        @Override
        public void execute() {
            if (!pendingResourceCalls.isEmpty()) {
                demandHandler.accept(pendingResourceCalls.poll(), this);
            } else {
                onCompletionHandler.accept(this);
            }
        }

    }

}

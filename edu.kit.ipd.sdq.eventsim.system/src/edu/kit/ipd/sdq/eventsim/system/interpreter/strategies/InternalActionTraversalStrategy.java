package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
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
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.InterruptTraversal;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.TraverseNextAction;
import edu.kit.ipd.sdq.eventsim.interpreter.state.ITraversalStrategyState;
import edu.kit.ipd.sdq.eventsim.interpreter.state.InternalState;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.events.ResumeSeffTraversalEvent;
import edu.kit.ipd.sdq.eventsim.system.interpreter.SeffBehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

/**
 * This traversal strategy is responsible for {@link InternalAction}s.
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 * @author Thomas Zwickl
 * 
 */
public class InternalActionTraversalStrategy
        extends DecoratingTraversalStrategy<AbstractAction, InternalAction, Request, RequestState> {

    private static final Logger logger = Logger.getLogger(InternalActionTraversalStrategy.class);

    private static final String PENDING_DEMANDS_PROPERTY = "pendingDemands";

    private static final String PENDING_RESOURCE_CALLS = "pendingResourceCalls";

    @Inject
    private IActiveResource activeResourceComponent;

    @Inject
    private ISimulationModel model;

    @Inject
    private SeffBehaviourInterpreter interpreter;

    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(final InternalAction action,
            final Request request, final RequestState state) {
        traverseDecorated(action, request, state);

        // restore or create state
        ITraversalStrategyState internalState = state.getInternalState(action);
        if (internalState == null) { // TODO
            internalState = new InternalState();
            state.addInternalState(action, internalState);
        }

        // get or prepare pending demands (via internal state)
        if (!internalState.hasProperty(PENDING_DEMANDS_PROPERTY)) {
            Queue<ParametricResourceDemand> pendingDemands = new LinkedList<>();
            for (final ParametricResourceDemand d : action.getResourceDemand_Action()) {
                pendingDemands.add(d);
            }
            internalState.setProperty(PENDING_DEMANDS_PROPERTY, pendingDemands);
        }
        Queue<ParametricResourceDemand> pendingDemands = internalState.getProperty(PENDING_DEMANDS_PROPERTY,
                new LinkedList<>());

        // get or prepare pending resource calls (via internal state)
        if (!internalState.hasProperty(PENDING_RESOURCE_CALLS)) {
            Queue<ResourceCall> pendingResourceCalls = new LinkedList<>();
            for (final ResourceCall r : action.getResourceCall__Action()) {
                // ignore all resource calls that go to non-processing resource types
                ResourceType type = findResourceType(r);
                if (isProcessingResourceType(type)) {
                    pendingResourceCalls.add(r);
                }
            }
            internalState.setProperty(PENDING_RESOURCE_CALLS, pendingResourceCalls);
        }
        Queue<ResourceCall> pendingResourceCalls = internalState.getProperty(PENDING_RESOURCE_CALLS,
                new LinkedList<>());

        // TODO really necessary?
        request.setRequestState(state);

        double evaluatedDemand;
        ResourceType type;
        int resourceServiceId;
        if (!pendingDemands.isEmpty()) {
            final ParametricResourceDemand demand = pendingDemands.poll();
            if (demand == null) {
                logger.warn("Missing resource demand for " + PCMEntityHelper.toString(action));
                return new TraverseNextAction<>(action.getSuccessor_AbstractAction());
            }

            evaluatedDemand = NumberConverter.toDouble(state.getStoExContext()
                    .evaluate(demand.getSpecification_ParametericResourceDemand().getSpecification()));
            type = demand.getRequiredResource_ParametricResourceDemand();
            resourceServiceId = 1;
        } else if (!pendingResourceCalls.isEmpty()) {
            final ResourceCall resourceCall = pendingResourceCalls.poll();

            if (resourceCall == null) {
                logger.warn("Missing resource call for " + PCMEntityHelper.toString(action));
                return new TraverseNextAction<>(action.getSuccessor_AbstractAction());
            }

            evaluatedDemand = NumberConverter.toDouble(
                    state.getStoExContext().evaluate(resourceCall.getNumberOfCalls__ResourceCall().getSpecification()));

            // find the corresponding resource type to be invoked by the resource call
            type = findResourceType(resourceCall);

            // find and adjust service id of interface
            ResourceSignature resourceSignature = resourceCall.getSignature__ResourceCall();
            resourceServiceId = resourceSignature.getResourceServiceId();
        } else {
            throw new RuntimeException("Could not find pending demands. Looks like a programming error.");
        }

        // consume the resource demand
        this.activeResourceComponent.consume(request, state.getComponent().getResourceContainer().getSpecification(),
                type, evaluatedDemand, resourceServiceId);

        // has more demands or resource calls?
        if (!pendingDemands.isEmpty() || !pendingResourceCalls.isEmpty()) {
            request.passivate(new ResumeSeffTraversalEvent(this.model, state, this.interpreter));
            return new InterruptTraversal<>(action);
        } else {
            request.passivate(new ResumeSeffTraversalEvent(this.model, state, this.interpreter));
            return new InterruptTraversal<>(action.getSuccessor_AbstractAction());
        }

    }

    private ResourceType findResourceType(ResourceCall resourceCall) {
        ResourceInterface resourceInterface = resourceCall.getSignature__ResourceCall()
                .getResourceInterface__ResourceSignature();
        return activeResourceComponent.findResourceType(resourceInterface);
    }

    private boolean isProcessingResourceType(ResourceType type) {
        // TODO get rid of instanceof check, if possible
        return type instanceof ProcessingResourceType;
    }

}

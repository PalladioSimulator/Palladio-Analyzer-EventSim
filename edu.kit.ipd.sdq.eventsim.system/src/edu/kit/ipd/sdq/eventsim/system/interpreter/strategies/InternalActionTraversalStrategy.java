package edu.kit.ipd.sdq.eventsim.system.interpreter.strategies;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourcetype.ResourceType;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
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
 * 
 */
public class InternalActionTraversalStrategy
        implements ITraversalStrategy<AbstractAction, InternalAction, Request, RequestState> {

    private static Logger logger = Logger.getLogger(InternalActionTraversalStrategy.class);

    private static final String PENDING_DEMANDS_PROPERTY = "pendingDemands";

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
        // restore or create state
        ITraversalStrategyState internalState = state.getInternalState(action);
        if (internalState == null) { // TODO
            internalState = new InternalState();
            state.addInternalState(action, internalState);
        }

        if (!internalState.hasProperty(PENDING_DEMANDS_PROPERTY)) {
            Queue<ParametricResourceDemand> pendingDemands = new LinkedList<>();
            for (final ParametricResourceDemand d : action.getResourceDemand_Action()) {
                pendingDemands.add(d);
            }
            internalState.setProperty(PENDING_DEMANDS_PROPERTY, pendingDemands);
        }
        Queue<ParametricResourceDemand> pendingDemands = internalState.getProperty(PENDING_DEMANDS_PROPERTY,
                new LinkedList<>());

        // TODO really necessary?
        request.setRequestState(state);

        final ParametricResourceDemand demand = pendingDemands.poll();
        if (demand == null) {
            logger.warn("Missing resource demand for " + PCMEntityHelper.toString(action));
            return new TraverseNextAction<>(action.getSuccessor_AbstractAction());
        }

        double evaluatedDemand = NumberConverter.toDouble(state.getStoExContext()
                .evaluate(demand.getSpecification_ParametericResourceDemand().getSpecification()));
        ResourceType type = demand.getRequiredResource_ParametricResourceDemand();

        // consume the resource demand
        activeResourceComponent.consume(request, state.getComponent().getResourceContainer().getSpecification(), type,
                evaluatedDemand);

        if (!pendingDemands.isEmpty()) { // has more demands
            request.passivate(new ResumeSeffTraversalEvent(model, state, interpreter));
            return new InterruptTraversal<>(action);
        } else {
            request.passivate(new ResumeSeffTraversalEvent(model, state, interpreter));
            return new InterruptTraversal<>(action.getSuccessor_AbstractAction());
        }

    }

}

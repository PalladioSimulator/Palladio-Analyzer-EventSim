package edu.kit.ipd.sdq.eventsim.system.events;

import javax.inject.Inject;

import org.palladiosimulator.pcm.seff.ForkedBehaviour;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.system.entities.ForkedRequest;
import edu.kit.ipd.sdq.eventsim.system.interpreter.SeffBehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

/**
 * Schedule this event to begin the traversal of a {@link ForkedBehaviour}.
 * <p>
 * The {@link ForkedRequest} that is supposed to traverse the behaviour, is passed to the {@code
 * schedule()} method.
 * 
 * @author Philipp Merkle
 * 
 */
public class BeginForkedBehaviourTraversalEvent extends AbstractSimEventDelegator<ForkedRequest> {

    private final ForkedBehaviour behaviour;
    private final RequestState parentState;

    @Inject
    private SeffBehaviourInterpreter interpreter;
    
    /**
     * Use this constructor to begin the traversal of the specified forked bheaviour.
     * 
     * @param model
     *            the model
     * 
     * @param parentState
     *            the state of the usage traversal
     */
    public BeginForkedBehaviourTraversalEvent(final ISimulationModel model, final ForkedBehaviour behaviour, RequestState parentState) {
        super(model, "BeginUsageTraversalEvent");
        this.behaviour = behaviour;
        this.parentState = parentState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventRoutine(final ForkedRequest who) {
        interpreter.beginTraversal(who, this.behaviour, this.parentState);
    }

}

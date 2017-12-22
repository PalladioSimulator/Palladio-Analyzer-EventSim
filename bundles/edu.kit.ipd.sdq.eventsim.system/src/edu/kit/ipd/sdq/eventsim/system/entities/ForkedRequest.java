package edu.kit.ipd.sdq.eventsim.system.entities;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ForkedBehaviour;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.interpreter.state.EntityState;

public class ForkedRequest extends Request {

    private Request parent;

    private boolean asynchronous;

    private final ForkedBehaviour behaviour;

    @Inject
    public ForkedRequest(final ISimulationModel model, @Assisted ForkedBehaviour behaviour,
            @Assisted boolean asynchronous, @Assisted Request parent) {
        super(model, parent.getSystemCall(), parent.getUser());
        this.behaviour = behaviour;
        this.asynchronous = asynchronous;
        this.parent = parent;

        // copy entity state using copy constructor
        this.setRequestState(new EntityState<AbstractAction>(parent.getRequestState()));
//        this.getRequestState().pushStackFrame();
    }

    public Request getParent() {
        return this.parent;
    }

    public boolean isAsynchronous() {
        return this.asynchronous;
    }

    public ForkedBehaviour getBehaviour() {
        return behaviour;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "ForkedRequest#" + this.getEntityId() + " of " + this.parent.getName();
    }

}

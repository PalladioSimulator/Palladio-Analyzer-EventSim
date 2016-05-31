package edu.kit.ipd.sdq.eventsim.system.entities;

import org.palladiosimulator.pcm.seff.ForkedBehaviour;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;

public class ForkedRequest extends Request {

    private Request parent;
    private boolean asynchronous;

    private final ForkedBehaviour behaviour;

    public ForkedRequest(final ISimulationModel model, ForkedBehaviour behaviour, boolean asynchronous, Request parent) {
        super(model, parent.getSystemCall(), parent.getUser());
        this.behaviour = behaviour;
        this.asynchronous = asynchronous;
        this.parent = parent;
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

package edu.kit.ipd.sdq.eventsim.system.debug;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.interpreter.listener.ITraversalListener;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

public class SimSlowdown implements ITraversalListener<AbstractAction, Request> {

    private static final int SLEEP_TIME = 5000;

    private String id;
    private boolean active;

    public SimSlowdown(String id) {
        this.id = id;
    }

    @Override
    public void before(AbstractAction action, Request entity) {
        if (!active) {
            if (action.getId().equals(id)) {
                active = true;
            }
        } else {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void after(AbstractAction action, Request entity) {
        if (active) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (action.getId().equals(this.id)) {
                active = false;
            }
        }

    }

}

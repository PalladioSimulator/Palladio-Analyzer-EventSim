package edu.kit.ipd.sdq.eventsim.system;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

public class SystemMeasurementConfiguration implements ProbeConfiguration {

    private TraversalListenerRegistry<AbstractAction, Request> listenerRegistry;

    public SystemMeasurementConfiguration(TraversalListenerRegistry<AbstractAction, Request> listenerRegistry) {
        this.listenerRegistry = listenerRegistry;
    }

    public TraversalListenerRegistry<AbstractAction, Request> getInterpreterConfiguration() {
        return listenerRegistry;
    }

}

package edu.kit.ipd.sdq.eventsim.system;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

public class SystemMeasurementConfiguration implements ProbeConfiguration {

    private TraversalListenerRegistry<AbstractAction, Request, RequestState> listenerRegistry;
    
	public SystemMeasurementConfiguration(TraversalListenerRegistry<AbstractAction, Request, RequestState> listenerRegistry) {
		this.listenerRegistry = listenerRegistry;
	}

	public TraversalListenerRegistry<AbstractAction, Request, RequestState> getInterpreterConfiguration() {
		return listenerRegistry;
	}

}

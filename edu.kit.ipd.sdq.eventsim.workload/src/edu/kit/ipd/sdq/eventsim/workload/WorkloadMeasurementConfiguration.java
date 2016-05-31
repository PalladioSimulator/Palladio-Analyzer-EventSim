package edu.kit.ipd.sdq.eventsim.workload;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

public class WorkloadMeasurementConfiguration implements ProbeConfiguration {

    private TraversalListenerRegistry<AbstractUserAction, User, UserState> traversalListeners;

    public WorkloadMeasurementConfiguration(
            TraversalListenerRegistry<AbstractUserAction, User, UserState> traversalListeners) {
        this.traversalListeners = traversalListeners;
    }

    public TraversalListenerRegistry<AbstractUserAction, User, UserState> getInterpreterConfiguration() {
        return traversalListeners;
    }

}

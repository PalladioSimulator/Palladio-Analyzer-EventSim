package edu.kit.ipd.sdq.eventsim.workload.probes;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.api.events.WorkloadUserFinishedEvent;
import edu.kit.ipd.sdq.eventsim.api.events.WorkloadUserSpawnEvent;
import edu.kit.ipd.sdq.eventsim.api.events.IEventHandler.Registration;
import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Probe;
import edu.kit.ipd.sdq.eventsim.measurement.probe.AbstractProbe;
import edu.kit.ipd.sdq.eventsim.workload.WorkloadMeasurementConfiguration;

@Probe(type = UsageScenario.class, property = "active_users")
public class ActiveUsersProbe<E extends UsageScenario> extends AbstractProbe<E, WorkloadMeasurementConfiguration> {

    private int activeUsers;

    public ActiveUsersProbe(MeasuringPoint<E> p, WorkloadMeasurementConfiguration cfg) {
        super(p, cfg);

        configuration.getMiddleware().registerEventHandler(WorkloadUserSpawnEvent.class, event -> {
            IUser user = event.getUser();

            // check if this probe is responsible for the present user's usage scenario
            // TODO use event filters to notify only relevant/interested handlers
            UsageScenario actualScenario = user.getUsageScenario();
            UsageScenario relevantScenario = p.getElement();
            if (actualScenario.getId().equals(relevantScenario.getId())) {
                activeUsers++;
                double simTime = cfg.getSimulationModel().getSimulationControl().getCurrentSimulationTime();

                // build measurement
                Measurement<E> m = new Measurement<>("ACTIVE_USERS", getMeasuringPoint(), user, activeUsers, simTime);

                // store
                // cache.put(m);

                // notify
                measurementListener.forEach(l -> l.notify(m));
            }
            return Registration.KEEP_REGISTERED;
        });
        
        configuration.getMiddleware().registerEventHandler(WorkloadUserFinishedEvent.class, event -> {
            IUser user = event.getUser();

            // check if this probe is responsible for the present user's usage scenario
            // TODO use event filters to notify only relevant/interested handlers
            UsageScenario actualScenario = user.getUsageScenario();
            UsageScenario relevantScenario = p.getElement();
            if (actualScenario.getId().equals(relevantScenario.getId())) {
                activeUsers--;
                double simTime = cfg.getSimulationModel().getSimulationControl().getCurrentSimulationTime();

                // build measurement
                Measurement<E> m = new Measurement<>("ACTIVE_USERS", getMeasuringPoint(), user, activeUsers, simTime);

                // store
                // cache.put(m);

                // notify
                measurementListener.forEach(l -> l.notify(m));
            }
            return Registration.KEEP_REGISTERED;
        });

    }

}

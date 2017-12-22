package edu.kit.ipd.sdq.eventsim.resources.probes.active;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Probe;
import edu.kit.ipd.sdq.eventsim.measurement.probe.AbstractProbe;
import edu.kit.ipd.sdq.eventsim.resources.ResourceProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.resources.entities.AbstractActiveResource;
import edu.kit.ipd.sdq.eventsim.resources.listener.IStateListener;

@Probe(type = AbstractActiveResource.class, property = "queue_length")
public class QueueLengthProbe extends AbstractProbe<AbstractActiveResource, ResourceProbeConfiguration> {

    public QueueLengthProbe(MeasuringPoint<AbstractActiveResource> p, ResourceProbeConfiguration configuration) {
        super(p, configuration);

        AbstractActiveResource resource = p.getElement();
        for (int instance = 0; instance < resource.getNumberOfInstances(); instance++) {
            resource.addStateListener(new IStateListener() {

                // TODO account for instanceid in measuring point (property suffix? explicit
                // objects?)
                @Override
                public void stateChanged(long state, int instanceId) {
                    // build measurement
                    double simTime = resource.getModel().getSimulationControl().getCurrentSimulationTime();
                    Measurement<AbstractActiveResource> m = new Measurement<>("QUEUE_LENGTH", getMeasuringPoint(), null,
                            state, simTime);

                    // store
                    // cache.put(m); TODO cache not needed! --> account for in abstract
                    // superclass/constructor? or
                    // enable by calculator (!)?

                    // notify
                    measurementListener.forEach(l -> l.notify(m));
                }
            }, instance);
        }

    }

}

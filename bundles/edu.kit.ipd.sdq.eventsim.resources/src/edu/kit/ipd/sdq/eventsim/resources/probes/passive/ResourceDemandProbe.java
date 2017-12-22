package edu.kit.ipd.sdq.eventsim.resources.probes.passive;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Probe;
import edu.kit.ipd.sdq.eventsim.measurement.probe.AbstractProbe;
import edu.kit.ipd.sdq.eventsim.resources.ResourceProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimPassiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimulatedProcess;
import edu.kit.ipd.sdq.eventsim.resources.listener.IPassiveResourceListener;

@Probe(type = SimPassiveResource.class, property = "resource_demand")
public class ResourceDemandProbe extends AbstractProbe<SimPassiveResource, ResourceProbeConfiguration> {

    public ResourceDemandProbe(MeasuringPoint<SimPassiveResource> p, ResourceProbeConfiguration configuration) {
        super(p, configuration);

        SimPassiveResource resource = p.getElement();
        resource.addListener(new IPassiveResourceListener() {

            @Override
            public void request(SimulatedProcess process, long num) {
                // build measurement
                double simTime = resource.getModel().getSimulationControl().getCurrentSimulationTime();
                Measurement<SimPassiveResource> m = new Measurement<>("RESOURCE_DEMAND", getMeasuringPoint(), process,
                        num, simTime);

                // notify
                measurementListener.forEach(l -> l.notify(m));
            }

            @Override
            public void release(SimulatedProcess process, long num) {
                // not relevant for this probe
            }

            @Override
            public void acquire(SimulatedProcess process, long num) {
                // not relevant for this probe
            }
        });
    }

}

package edu.kit.ipd.sdq.eventsim.resources.probes.active;

import de.uka.ipd.sdq.scheduler.ISchedulableProcess;
import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Probe;
import edu.kit.ipd.sdq.eventsim.measurement.probe.AbstractProbe;
import edu.kit.ipd.sdq.eventsim.resources.ResourceProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimActiveResource;
import edu.kit.ipd.sdq.eventsim.resources.listener.IDemandListener;

@Probe(type = SimActiveResource.class, property = "resource_demand")
public class ResourceDemandProbe extends AbstractProbe<SimActiveResource, ResourceProbeConfiguration> {

	public ResourceDemandProbe(MeasuringPoint<SimActiveResource> p, ResourceProbeConfiguration configuration) {
		super(p, configuration);

		SimActiveResource resource = p.getElement();
		for (int instance = 0; instance < resource.getNumberOfInstances(); instance++) {
			resource.addDemandListener(new IDemandListener() {
				// TODO account for instanceid in measuring point (property suffix? explicit objects?)
				@Override
				public void demand(ISchedulableProcess process, double demand) {
					// build measurement
					double simTime = resource.getModel().getSimulationControl().getCurrentSimulationTime();
					Measurement<SimActiveResource> m = new Measurement<>("RESOURCE_DEMAND",
							getMeasuringPoint(), process, demand, simTime);

					// store
					// cache.put(m); TODO cache not needed! --> account for in abstract superclass/constructor? or
					// enable by calculator (!)?

					// notify
					measurementListener.forEach(l -> l.notify(m));
				}
			});
		}

	}

}

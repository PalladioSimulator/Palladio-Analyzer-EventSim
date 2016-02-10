package edu.kit.ipd.sdq.eventsim.measurement.probe;

import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;

public interface ProbeLocator<C extends ProbeConfiguration> {

	public Class<? extends AbstractProbe<?, ?, C>> probeForType(Class<?> type, String property);
	
}

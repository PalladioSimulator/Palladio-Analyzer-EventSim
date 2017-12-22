package edu.kit.ipd.sdq.eventsim.api.events;

import java.util.Collections;
import java.util.Map;

public class AbstractSimulationEvent implements SimulationEvent {

	@Override
	public Map<String, String> getProperties() {
		return Collections.emptyMap();
	}

}

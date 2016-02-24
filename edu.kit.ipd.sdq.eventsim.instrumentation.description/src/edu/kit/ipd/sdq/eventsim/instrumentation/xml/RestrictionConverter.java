package edu.kit.ipd.sdq.eventsim.instrumentation.xml;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;

public interface RestrictionConverter {

	AdaptedInstrumentableRestriction fromImplementation(InstrumentableRestriction<?> restriction);

	InstrumentableRestriction<?> fromAdaption(AdaptedInstrumentableRestriction adapted);

}

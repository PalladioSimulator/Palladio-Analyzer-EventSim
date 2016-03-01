package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.useraction;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.AdaptedInstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionConverter;

public class UserActionTypeExclusionConverter implements RestrictionConverter {

	@Override
	public AdaptedInstrumentableRestriction fromImplementation(InstrumentableRestriction<?> restriction) {
		UserActionTypeExclusion exclusion = (UserActionTypeExclusion) restriction;
		AdaptedInstrumentableRestriction adapted = new AdaptedInstrumentableRestriction();
		adapted.setType(exclusion.getClass());
		adapted.addElement("user-action-type", Class.class, exclusion.getExcludedType().getName());
		return adapted;
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstrumentableRestriction<?> fromAdaption(AdaptedInstrumentableRestriction adapted) {
		try {
			return new UserActionTypeExclusion((Class<? extends AbstractUserAction>) Class
					.forName(adapted.getValue("user-action-type", Class.class)));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

}

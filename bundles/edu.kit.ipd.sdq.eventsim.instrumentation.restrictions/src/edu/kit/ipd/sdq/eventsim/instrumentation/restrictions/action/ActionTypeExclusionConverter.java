package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.AdaptedInstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionConverter;

public class ActionTypeExclusionConverter implements RestrictionConverter {

	@Override
	public AdaptedInstrumentableRestriction fromImplementation(InstrumentableRestriction<?> restriction) {
		ActionTypeExclusion exclusion = (ActionTypeExclusion) restriction;
		AdaptedInstrumentableRestriction adapted = new AdaptedInstrumentableRestriction();
		adapted.setType(exclusion.getClass());
		adapted.addElement("action-type", Class.class, exclusion.getExcludedType().getName());
		return adapted;
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstrumentableRestriction<?> fromAdaption(AdaptedInstrumentableRestriction adapted) {
		try {
			return new ActionTypeExclusion(
					(Class<? extends AbstractAction>) Class.forName(adapted.getValue("action-type", Class.class)));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

}

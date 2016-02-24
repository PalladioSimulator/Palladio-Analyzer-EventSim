package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.AdaptedInstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionConverter;

public class SingleActionRestrictionConverter implements RestrictionConverter {

	@Override
	public AdaptedInstrumentableRestriction fromImplementation(InstrumentableRestriction<?> restriction) {
		SingleActionRestriction<?> res = (SingleActionRestriction<?>) restriction;
		AdaptedInstrumentableRestriction a = new AdaptedInstrumentableRestriction();
		a.setType(res.getClass());
		a.addElement(new AdaptedInstrumentableRestriction.Element("id", String.class, res.getActionId()));
		a.addElement(new AdaptedInstrumentableRestriction.Element("type", Class.class, res.getActionType().getName()));
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstrumentableRestriction<?> fromAdaption(AdaptedInstrumentableRestriction adapted) {
		try {
			return new SingleActionRestriction<>(adapted.getValue("id", String.class),
					(Class<? extends AbstractAction>) Class.forName(adapted.getValue("type", Class.class)));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.useraction;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.AdaptedInstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionConverter;

public class SingleUserActionRestrictionConverter implements RestrictionConverter {

	@Override
	public AdaptedInstrumentableRestriction fromImplementation(InstrumentableRestriction<?> restriction) {
		SingleUserActionRestriction<?> res = (SingleUserActionRestriction<?>) restriction;
		AdaptedInstrumentableRestriction a = new AdaptedInstrumentableRestriction();
		a.setType(res.getClass());
		a.addElement(new AdaptedInstrumentableRestriction.Element("id", String.class, res.getUserActionId()));
		a.addElement(
				new AdaptedInstrumentableRestriction.Element("type", Class.class, res.getUserActionType().getName()));
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public InstrumentableRestriction<?> fromAdaption(AdaptedInstrumentableRestriction adapted) {
		try {
			return new SingleUserActionRestriction<>(adapted.getValue("id", String.class),
					(Class<? extends AbstractUserAction>) Class.forName(adapted.getValue("type", Class.class)));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.xml;

import java.lang.reflect.Constructor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;

public class RestrictionXmlAdapter extends XmlAdapter<AdaptedInstrumentableRestriction, InstrumentableRestriction<?>> {

	@Override
	public AdaptedInstrumentableRestriction marshal(InstrumentableRestriction<?> restriction) throws Exception {
		Restriction a = restriction.getClass().getAnnotation(Restriction.class);

		if (a == null) {
			throw new IllegalArgumentException();
		}

		Constructor<? extends RestrictionConverter> c = a.converter().getConstructor();
		RestrictionConverter converter = c.newInstance();
		return converter.fromImplementation(restriction);
	}

	@Override
	public InstrumentableRestriction<?> unmarshal(AdaptedInstrumentableRestriction adapted) throws Exception {
		Restriction a = adapted.getType().getAnnotation(Restriction.class);

		if (a == null) {
			throw new IllegalArgumentException();
		}

		Constructor<? extends RestrictionConverter> c = a.converter().getConstructor();
		RestrictionConverter converter = c.newInstance();
		return converter.fromAdaption(adapted);
	}

}

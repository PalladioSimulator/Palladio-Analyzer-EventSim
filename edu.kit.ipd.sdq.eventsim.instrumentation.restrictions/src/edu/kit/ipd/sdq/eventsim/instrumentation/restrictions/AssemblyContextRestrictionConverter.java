package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.AdaptedInstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionConverter;

public class AssemblyContextRestrictionConverter implements RestrictionConverter {

	@Override
	public AdaptedInstrumentableRestriction fromImplementation(InstrumentableRestriction<?> restriction) {
		AssemblyContextRestriction<?> res = (AssemblyContextRestriction<?>) restriction;

		AdaptedInstrumentableRestriction adapted = new AdaptedInstrumentableRestriction();
		adapted.setType(res.getClass());
		adapted.addElement("assembly-context", AssemblyContext.class, res.getAssemblyContextId());

		return null;
	}

	@Override
	public InstrumentableRestriction<?> fromAdaption(AdaptedInstrumentableRestriction adapted) {
		AssemblyContextRestriction<?> restriction = null;

		try {
			@SuppressWarnings("unchecked")
			Constructor<? extends AssemblyContextRestriction<?>> c = (Constructor<? extends AssemblyContextRestriction<?>>) adapted
					.getType().getConstructor();
			restriction = c.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		restriction.setAssemblyContextId(adapted.getValue("assembly-context", AssemblyContext.class));

		return null;
	}

}

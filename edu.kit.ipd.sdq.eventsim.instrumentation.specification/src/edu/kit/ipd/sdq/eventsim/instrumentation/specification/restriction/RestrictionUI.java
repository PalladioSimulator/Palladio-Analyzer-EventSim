package edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;

/**
 * States that the annotated UI is able to deal with the specified subtype of
 * {@link InstrumentableRestriction}.
 * 
 * @author Henning Schulz
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestrictionUI {

	@SuppressWarnings("rawtypes")
	Class<? extends InstrumentableRestriction> restrictionType();

}

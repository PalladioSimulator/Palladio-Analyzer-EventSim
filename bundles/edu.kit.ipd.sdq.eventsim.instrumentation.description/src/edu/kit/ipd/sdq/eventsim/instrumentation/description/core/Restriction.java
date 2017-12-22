package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionConverter;

/**
 * 
 * An annotation to mark implementations of {@link InstrumentableRestriction}.
 * It holds a name for visualization purposes and the type of elements the
 * restriction can deal with.
 * 
 * @author Henning Schulz
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Restriction {

	String name();

	Class<? extends Instrumentable> instrumentableType();

	Class<? extends RestrictionConverter> converter();

}

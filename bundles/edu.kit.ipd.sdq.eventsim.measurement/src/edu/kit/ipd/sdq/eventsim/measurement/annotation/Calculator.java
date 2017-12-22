package edu.kit.ipd.sdq.eventsim.measurement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.kit.ipd.sdq.eventsim.measurement.Pair;

/**
 * Declares that the annotated class is able to calculate the specified metric
 * on the specified type. The fields {@code fromType} and {@code toType} are
 * intended to be used if {@code type} is a {@link Pair}. <br>
 * 
 * The intended probes list all expedient combinations of probes.
 * 
 * @author Henning Schulz
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Calculator {

	Class<?> type();

	Class<?> fromType() default Void.class;

	Class<?> toType() default Void.class;

	String metric();

	ProbePair[] intendedProbes();

}

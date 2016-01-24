package edu.kit.ipd.sdq.eventsim.measurement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Calculator {

	Class<?> type();

	Class<?> fromType() default Void.class;

	Class<?> toType() default Void.class;

	String metric();

}

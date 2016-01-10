package edu.kit.ipd.sdq.eventsim.measurement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Calculator {
	
	Class<?> type();
	Class<?> fromType() default Object.class;
	Class<?> toType() default Object.class;
	
	String property();
	
}

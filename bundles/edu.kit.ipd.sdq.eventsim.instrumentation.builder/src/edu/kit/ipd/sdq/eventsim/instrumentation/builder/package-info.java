/**
 * This package contains a builder for
 * {@link edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription
 * InstrumentationDescription}s. It should be used as in the following example:
 * <br>
 * 
 * {@code new InstrumentationDescriptionBuilder().newActionRule(ExternalCallAction.class).underRestriction(myRestriction).addProbe("before").addProbe("after").addCalculator("responsetime").from("before").to("after").ruleDone().build;}
 */
package edu.kit.ipd.sdq.eventsim.instrumentation.builder;
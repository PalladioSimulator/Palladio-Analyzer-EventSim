/**
 * This package provides means for injection of instrumentations based on
 * {@link edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription
 * InstrumentationDescription}s. The
 * {@link edu.kit.ipd.sdq.eventsim.instrumentation.injection.InstrumentorBuilder
 * InstrumentorBuilder} can be used to create an
 * {@link edu.kit.ipd.sdq.eventsim.instrumentation.injection.Instrumentor
 * Instrumentor} which is actually responsible for instrumentation injection.
 * Using the builder, the actual implementation of the instrumentor does not
 * need to be known.<br>
 * An example for the usage of the builder and the instrumentors is provided in
 * the following:
 * 
 * <pre>
 * private ActiveResourceRep getModelResource(MyResourceImpl res) {
 * 	// do something
 * 	return modelResource;
 * }
 * 
 * ...
 * 
 * {@code Instrumentor<MyResourceImpl, ?> instrumentor =
 * 	InstrumentorBuilder.buildFor(myPcm)}
 * 		.withDescription(myDescription)
 * 		.inBundle(myBundle)
 * 		.withStorage(myStorage)
 * 		.forModelType(ActiveResourceRep.class)
 * 		.withMapping(this::getModelResource)
 * 		.createFor(myProbeConfig);
 * 
 * ...
 * 
 * MyResourceImpl res = ...;
 * instrumentor.instrument(res);
 * </pre>
 */
package edu.kit.ipd.sdq.eventsim.instrumentation.injection;
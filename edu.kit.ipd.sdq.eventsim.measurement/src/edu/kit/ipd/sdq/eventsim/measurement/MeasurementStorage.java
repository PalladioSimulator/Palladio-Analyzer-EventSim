package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.function.Function;

import org.palladiosimulator.pcm.core.entity.Entity;

public interface MeasurementStorage {

	IdProvider getIdProvider();

	void addIdProvider(Class<? extends Object> elementClass, Function<Object, String> extractionFunction);

	<E> void put(Measurement<E, ?> m);

	<F extends Entity, S extends Entity, T> void putPair(Measurement<Pair<F, S>, T> m);

	void finish();

}
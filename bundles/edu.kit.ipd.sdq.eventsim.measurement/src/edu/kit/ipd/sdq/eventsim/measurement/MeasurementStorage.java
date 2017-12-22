package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.List;
import java.util.function.Function;

public interface MeasurementStorage {

    /**
     * Adds a mapping (by means of a {@link Function}) from {@link Object}s of a fixed type to
     * {@link String}s that uniquely identify (id) objects of the specified {@code type}. This
     * allows to extract ids from arbitrary {@link Object}s without dictating an interface.
     * 
     * @param type
     *            the type's class
     * @param extractionFunction
     *            the function that maps {@code type} instances to id-Strings.
     */
    void addIdExtractor(Class<? extends Object> type, Function<Object, String> extractionFunction);

    /**
     * @see #addIdExtractor(Class, Function)
     * 
     * @param type
     *            the type's class
     * @param extractionFunction
     *            the function that maps {@code type} instances to name-Strings.
     */
    void addNameExtractor(Class<? extends Object> type, Function<Object, String> extractionFunction);

    /**
     * @see #addIdExtractor(Class, Function)
     * 
     * @param type
     *            the type's class
     * @param extractionFunction
     *            the function that maps {@code type} instances to type-Strings.
     */
    void addTypeExtractor(Class<? extends Object> elementClass, Function<Object, String> extractionFunction);

    /**
     * Stores the given measurement.
     * 
     * @param m
     *            the measurement to be added
     * @param <E>
     *            the measuring point's type (i.e. the type of the probed element)
     */
    void put(Measurement<?> m);

    /**
     * Adds global metadata that will be appended to each {@link Measurement} added via
     * {@link #put(Measurement)}.
     * 
     * @param metadata
     *            the global metadata to be appended
     */
    void addMetadata(Metadata... metadata);

    /**
     * @see #addMetadata(Metadata...)
     */
    void addMetadata(List<Metadata> metadata);

    void start() throws MeasurementStorageStartException;

    void finish();

}
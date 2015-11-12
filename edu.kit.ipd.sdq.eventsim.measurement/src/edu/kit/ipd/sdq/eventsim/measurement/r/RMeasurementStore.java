package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.FinalizeRProcessingJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.PushBufferToRJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.StoreRDSFileJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.launch.RConfigurationConstants;

/**
 * Stores {@link Measurement}s into R using Rserve (for details on Rserve see https://rforge.net/Rserve).
 * <p>
 * Measurements are buffered and sent to R as a batch once the buffer size reaches its capacity. Increasing the buffer
 * capacity improves performance at the cost of higher memory consumption (this needs to be further evaluated, however).
 * 
 * @author Philipp Merkle
 *
 */
public class RMeasurementStore {

	static final Logger log = Logger.getLogger(RMeasurementStore.class);

	private static final int BUFFER_CAPACITY = 10_000;

	private Buffer buffer;

	/** the number of measurements processed since the last reset (or instantiation) */
	private int processed;

	private IdProvider idProvider;

	private RJobProcessor rJobProcessor;

	private int bufferNumber;

	private boolean storeRds;
	private String rdsFilePath;

	/**
	 * Use this constructor when no RDS file is to be created upon finish.
	 */
	public RMeasurementStore() {
		this("");
		this.storeRds = false;
	}

	/**
	 * Use this constructor when an RDS file is to be created upon finish.
	 * 
	 * @param rdsFilePath
	 *            the location of the file to be created.
	 */
	public RMeasurementStore(String rdsFilePath) {
		this.storeRds = true;
		this.rdsFilePath = rdsFilePath;
		idProvider = new IdProvider();
		rJobProcessor = new RJobProcessor();
		rJobProcessor.start();
		buffer = new Buffer(BUFFER_CAPACITY, idProvider);
	}

	/**
	 * Constructs a {@link RMeasurementStore} by extracting configuration options from the provided launch
	 * configuration.
	 * 
	 * @param configuration
	 *            the launch configuration
	 * @return the constructed {@link RMeasurementStore}, or {@code null} if expected configuration options could not be
	 *         found in the provided launch configuration.
	 */
	public static RMeasurementStore fromLaunchConfiguration(Map<String, Object> configuration) {
		Boolean createRds = (Boolean) configuration.get(RConfigurationConstants.CREATE_RDS_FILE_KEY);
		if (!createRds) {
			return new RMeasurementStore();
		}
		String rdsFilePath = (String) configuration.get(RConfigurationConstants.RDS_FILE_PATH_KEY);
		if (rdsFilePath != null) {
			return new RMeasurementStore(rdsFilePath);
		}
		return null;
	}

	public IdProvider getIdProvider() {
		return idProvider;
	}

	public void addIdProvider(Class<? extends Object> elementClass, Function<Object, String> extractionFunction) {
		idProvider.add(elementClass, extractionFunction);
	}

	public <E> void put(Measurement<E, ?> m) {
		buffer.put(m);
		if (buffer.isFull()) {
			rJobProcessor.enqueue(new PushBufferToRJob(buffer, bufferNumber++));
			buffer = new Buffer(BUFFER_CAPACITY, idProvider);
		}
	}

	public <F extends Entity, S extends Entity, T> void putPair(Measurement<Pair<F, S>, T> m) {
		buffer.putPair(m);
		if (buffer.isFull()) {
			rJobProcessor.enqueue(new PushBufferToRJob(buffer, bufferNumber++));
			buffer = new Buffer(BUFFER_CAPACITY, idProvider);
		}
	}

	public void finish() {
		buffer.shrinkToSize();

		rJobProcessor.enqueue(new PushBufferToRJob(buffer, bufferNumber++));
		if (storeRds) {
			rJobProcessor.enqueue(new StoreRDSFileJob(rdsFilePath));
		} else {
			log.info("Skipping creation of RDS file.");
		}
		rJobProcessor.enqueue(new FinalizeRProcessingJob());

		// wait until R processing is finished
		rJobProcessor.waitUntilFinished();

		// clean up
		buffer = new Buffer(BUFFER_CAPACITY, idProvider);
		bufferNumber = 0;
		processed = 0;
	}

}

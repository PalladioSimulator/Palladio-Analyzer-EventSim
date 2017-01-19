package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorageStartException;
import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.measurement.PropertyExtractor;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.FinalizeRProcessingJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.MergeBufferedDataFramesJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.PushBufferToRJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.StoreRDSFileJob;

/**
 * Stores {@link Measurement}s into R using Rserve (for details on Rserve see
 * https://rforge.net/Rserve).
 * <p>
 * Measurements are buffered and sent to R as a batch once the buffer size reaches its capacity.
 * Increasing the buffer capacity improves performance at the cost of higher memory consumption
 * (this needs to be further evaluated, however).
 * 
 * @author Philipp Merkle
 *
 */
public class RMeasurementStore implements MeasurementStorage {

    private static final Logger log = Logger.getLogger(RMeasurementStore.class);

    private static final int BUFFER_CAPACITY = 10_000;

    private Buffer buffer;

    private PropertyExtractor idExtractor;

    private PropertyExtractor nameExtractor;

    private PropertyExtractor typeExtractor;

    private RserveConnection connection;

    private RJobProcessor rJobProcessor;

    private int bufferNumber;

    private boolean storeRds;
    private String rdsFilePath;

    private Metadata[] globalMetadata;

    private ReentrantReadWriteLock bufferLock = new ReentrantReadWriteLock(true);

    /**
     * Use this constructor when no RDS file is to be created upon finish.
     */
    public RMeasurementStore(RserveConnection connection) {
        this(connection, "");
        this.connection = connection;
        this.storeRds = false;
    }

    /**
     * Use this constructor when an RDS file is to be created upon finish.
     * 
     * @param rdsFilePath
     *            the location of the file to be created.
     */
    public RMeasurementStore(RserveConnection connection, String rdsFilePath) {
        this.connection = connection;
        this.storeRds = true;
        this.rdsFilePath = rdsFilePath;
        idExtractor = new PropertyExtractor();
        nameExtractor = new PropertyExtractor();
        typeExtractor = new PropertyExtractor();
        rJobProcessor = new RJobProcessor(connection);

        // add simple type extractor as a default
        typeExtractor.add(Object.class, new Function<Object, String>() {
            @Override
            public String apply(Object o) {
                return stripNamespace(o.getClass().getName());
            }

            private String stripNamespace(String fqn) {
                int startOfClassName = fqn.lastIndexOf(".");
                return fqn.substring(startOfClassName + 1, fqn.length());
            }
        });

        buffer = new Buffer(BUFFER_CAPACITY, idExtractor, nameExtractor, typeExtractor);
    }

    /**
     * Constructs a {@link RMeasurementStore} by extracting configuration options from the provided
     * launch configuration.
     * 
     * @param configuration
     *            the launch configuration
     * @return the constructed {@link RMeasurementStore}, or {@code null} if expected configuration
     *         options could not be found in the provided launch configuration.
     */
    public static RMeasurementStore fromLaunchConfiguration(Map<String, Object> configuration,
            RserveConnection connection) {
        Boolean createRds = (Boolean) configuration.get(RConfigurationConstants.CREATE_RDS_FILE_KEY);
        if (createRds == null || !createRds) {
            return new RMeasurementStore(connection);
        }
        String rdsFilePath = (String) configuration.get(RConfigurationConstants.RDS_FILE_PATH_KEY);
        if (rdsFilePath != null) {
            return new RMeasurementStore(connection, rdsFilePath);
        }
        return null;
    }

    @Override
    public void addIdExtractor(Class<? extends Object> elementClass, Function<Object, String> extractionFunction) {
        idExtractor.add(elementClass, extractionFunction);
    }

    @Override
    public void addNameExtractor(Class<? extends Object> elementClass, Function<Object, String> extractionFunction) {
        nameExtractor.add(elementClass, extractionFunction);
    }

    @Override
    public void addTypeExtractor(Class<? extends Object> elementClass, Function<Object, String> extractionFunction) {
        typeExtractor.add(elementClass, extractionFunction);
    }

    @Override
    public void put(Measurement<?> m) {
        /*
         * lock handling implemented as suggested by
         * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/
         * ReentrantReadWriteLock.html
         */
        bufferLock.readLock().lock();
        if (buffer.isFull()) {
            // must release read lock before acquiring write lock
            bufferLock.readLock().unlock();
            bufferLock.writeLock().lock();
            try {
                // recheck state because another thread might have acquired write lock and changed
                // state before we did
                if (buffer.isFull()) {
                    rJobProcessor.enqueue(new PushBufferToRJob(buffer, bufferNumber++));
                    buffer = new Buffer(BUFFER_CAPACITY, idExtractor, nameExtractor, typeExtractor);
                }
                // downgrade lock by acquiring read lock before releasing write lock
                bufferLock.readLock().lock();
            } finally {
                // release write lock, keep read lock
                bufferLock.writeLock().unlock();
            }
        }

        try {
            // add global metadata to measurement, if present
            if (globalMetadata != null) {
                m.addMetadata(globalMetadata);
            }
            boolean added = buffer.put(m);
            int retries = 0;
            while (!added && retries < 10) {
                added = buffer.put(m);
                retries++;
            }
            if (!added) {
                System.out.println("Failed to add measurement " + m + " to buffer " + bufferNumber + buffer
                        + " after multiple retries.");
                // log.warn("Failed to add measurement " + m + " to buffer after multiple
                // retries.");
            }
        } finally {
            bufferLock.readLock().unlock();
        }
    }

    @Override
    public void addMetadata(Metadata... metadata) {
        globalMetadata = metadata;
    }

    @Override
    public void addMetadata(List<Metadata> metadata) {
        globalMetadata = metadata.toArray(new Metadata[metadata.size()]);
    }

    @Override
    public void start() throws MeasurementStorageStartException {
        if (connection != null && connection.isConnected()) {
            rJobProcessor.start();
        } else {
            throw new MeasurementStorageStartException("No connection to R available. Run Rserve, "
                    + "e.g. by calling \"library(Rserve); Rserve()\" in R.");
        }
    }

    @Override
    public void finish() {
        log.info("Closing R measurement store...");
        bufferLock.writeLock().lock();
        try {
            buffer.shrinkToSize();

            rJobProcessor.enqueue(new PushBufferToRJob(buffer, bufferNumber++));
            rJobProcessor.enqueue(new MergeBufferedDataFramesJob());

            // handle R job extensions
            for (RJob job : JobExtensionHelper.createExtensionJobs()) {
                log.info("Processing R extension job: " + job.getName());
                rJobProcessor.enqueue(job);
            }

            if (storeRds) {
                rJobProcessor.enqueue(new StoreRDSFileJob(rdsFilePath));
            } else {
                log.info("Skipping creation of RDS file.");
            }
            rJobProcessor.enqueue(new FinalizeRProcessingJob());

            // wait until R processing is finished
            rJobProcessor.waitUntilFinished();

            // clean up
            // TODO really needed?
            buffer = new Buffer(BUFFER_CAPACITY, idExtractor, nameExtractor, typeExtractor);
            bufferNumber = 0;
        } finally {
            bufferLock.writeLock().unlock();
        }
    }

}

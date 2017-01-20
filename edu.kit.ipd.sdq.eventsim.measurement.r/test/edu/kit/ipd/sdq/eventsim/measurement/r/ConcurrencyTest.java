package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorageStartException;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;

/**
 * Tests {@link RMeasurementStore} under highly concurrent load. Checks that no measurement is lost
 * and that each measurement is consistent.
 * <p>
 * Requires an Rserve sever to be available on localhost:6311.
 * 
 * @author Philipp Merkle
 *
 */
public class ConcurrencyTest {

    private static final int PRODUCER_TRHEADS = 10;

    private AtomicInteger sequence = new AtomicInteger(0);

    private CountDownLatch startLatch = new CountDownLatch(1);

    private MeasurementStorage measurementStore;

    private boolean keepRunning = true;

    private List<Thread> threads = new LinkedList<>();

    @Test
    public void test()
            throws REXPMismatchException, EvaluationException, MeasurementStorageStartException, InterruptedException {
        // init logging
        BasicConfigurator.configure();

        RserveConnection connection = new RserveConnection();
        connection.connect("localhost", 6311);
        measurementStore = new RMeasurementStore(connection);
        measurementStore.addIdExtractor(String.class, s -> (String) s);
        measurementStore.addNameExtractor(String.class, s -> (String) s);
        measurementStore.addTypeExtractor(String.class, s -> String.class.getSimpleName());
        measurementStore.start();

        prepareThreads();
        long start = System.currentTimeMillis();
        startThreadsSimultaneously();
        Thread.sleep(300);
        stopThreads();
        measurementStore.finish();

        long end = System.currentTimeMillis();

        RList columnList = EvaluationHelper.evaluate(connection.getConnection(), "mm").asList();
        System.out.println(columnList.names);
        int[] values = columnList.at("value").asIntegers();
        String[] what = columnList.at("what").asStrings();
        double[] when = columnList.at("when").asDoubles();
        String[] where = columnList.at("where.first.id").asStrings();
        String[] who = columnList.at("who.id").asStrings();

        // check lengths
        Assert.assertEquals(sequence.get(), values.length);
        Assert.assertEquals(sequence.get(), what.length);
        Assert.assertEquals(sequence.get(), when.length);
        Assert.assertEquals(sequence.get(), where.length);
        Assert.assertEquals(sequence.get(), who.length);

        // for each entry
        Set<Integer> seenSequenceNumbers = new HashSet<>();
        for (int i = 0; i < sequence.get(); i++) {
            int value = values[i];

            // each sequence number needs to be unique
            Assert.assertFalse(seenSequenceNumbers.contains(value));
            seenSequenceNumbers.add(value);

            // check current entry's consistency
            Assert.assertTrue(what[i].endsWith(Integer.toString(value)));
            Assert.assertEquals((double) value, when[i], 0.000001);
            Assert.assertTrue(where[i].endsWith(Integer.toString(value)));
            Assert.assertTrue(who[i].endsWith(Integer.toString(value)));
        }

        System.out.println("Total measurements: " + values.length);
        System.out.println("Runtime (ms): " + (end - start));
    }

    private void startThreadsSimultaneously() {
        startLatch.countDown();
    }

    private void prepareThreads() {
        for (int i = 0; i < PRODUCER_TRHEADS; i++) {
            Thread t = new Thread(new MeasurementProducer());
            threads.add(t);
            t.start();
        }
    }

    private void stopThreads() {
        keepRunning = false;
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Measurement<?> createMeasurement(int value) {
        return new Measurement<>("what" + value, new MeasuringPoint<String>("measuringPoint" + value), "who" + value,
                value, value);
    }

    private class MeasurementProducer implements Runnable {

        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (keepRunning) {
                measurementStore.put(createMeasurement(sequence.getAndIncrement()));
            }
        }

    }

}

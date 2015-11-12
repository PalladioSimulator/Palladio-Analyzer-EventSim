package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.FinalizeRProcessingJob;

/**
 * Connects to R and processes enqueued {@link RJob}s in a FCFS manner.
 * 
 * @author Philipp Merkle
 *
 */
public class RJobProcessor {

	private static final Logger log = Logger.getLogger(RJobProcessor.class);

	private BlockingQueue<RJob> jobQueue;

	private Thread thread;

	public RJobProcessor() {
		this.jobQueue = new LinkedBlockingQueue<>();
	}

	/**
	 * Enqueues the given {@code job} for processing by this job processor.
	 * 
	 * @param job
	 *            the job to be scheduled for processing
	 */
	public void enqueue(RJob job) {
		try {
			jobQueue.put(job);
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	private RConnection connectToR() {
		try {
			RConnection connection = new RConnection();
			connection.voidEval("library(data.table)");
			return connection;
		} catch (RserveException e) {
			RMeasurementStore.log
					.error("Rserve reported an error in initialization. Check if the package \"data.table\" is "
							+ "installed in R.", e);
			return null; // TODO better throw exception
		}
	}

	/**
	 * Starts processing enqueued jobs.
	 */
	public void start() {
		thread = new Thread(new RJobProcessorRunnable());
		thread.start();
	}

	/**
	 * Waits until all jobs left in this processor's queue have been processed. This effectively stops the caller
	 * thread.
	 */
	public void waitUntilFinished() {
		if (thread == null) {
			throw new IllegalStateException("This job processor needs to be started first.");
		}
		try {
			thread.join();
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	private class RJobProcessorRunnable implements Runnable {
		@Override
		public void run() {
			RConnection connection = connectToR();
			RContext context = new RContext(connection);

			boolean keepRunning = true;
			while (keepRunning) {
				try {
					RJob job = jobQueue.take();

					long start = System.currentTimeMillis();
					// process job
					job.process(context);
					long end = System.currentTimeMillis();
					log.debug(String.format("%s. Took %.2f seconds.", job.getName(), (end - start) / 1000.0));
					context.getStatistics().captureTimeSpentInR(end - start);

					// shut down if "poison pill" has been processed
					if (job.getClass().equals(FinalizeRProcessingJob.class)) {
						keepRunning = false;
					}
				} catch (InterruptedException e) {
					RMeasurementStore.log.error(e);
				}
			}
			connection.close();
			RMeasurementStore.log.info(String.format("Finished R processing. Total time spent in R: %.2f seconds.",
					context.getStatistics().getTotalTimeSpentInR() / 1000.0));
		}
	}

}
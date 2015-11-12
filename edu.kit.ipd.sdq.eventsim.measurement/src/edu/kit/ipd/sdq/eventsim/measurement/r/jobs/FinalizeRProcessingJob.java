package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJobProcessor;

/**
 * Enqueue this "poison pill" with a {@link RJobProcessor} to shut down the processor.
 * 
 * @author Philipp Merkle
 */
public class FinalizeRProcessingJob implements RJob {

	@Override
	public void process(RContext context) {
		// nothing to do
	}

	@Override
	public String getName() {
		return "Finalize R processing";
	}

}
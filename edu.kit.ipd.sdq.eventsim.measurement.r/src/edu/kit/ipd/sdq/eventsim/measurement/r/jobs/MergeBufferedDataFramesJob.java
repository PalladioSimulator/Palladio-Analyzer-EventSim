package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;

/**
 * Creates a single data frame from multiple buffered data frames. 
 * 
 * @author Philipp Merkle
 *
 */
public class MergeBufferedDataFramesJob implements RJob {

	private static final Logger log = Logger.getLogger(MergeBufferedDataFramesJob.class);
	
	@Override
	public void process(RContext context) {
		try {
			context.getConnection().voidEval("mm <- rbindlist(mm)");
		} catch (RserveException e) {
			log.error(e);
		}
	}

	@Override
	public String getName() {
		return "Merge buffered data frames into single data frame";
	}

}

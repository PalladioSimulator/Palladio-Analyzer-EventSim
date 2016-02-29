package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import org.apache.log4j.Logger;

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
			// fill=TRUE allows to merge data.frames with different column count, as long as columns are named to allow
			// for matching them.   
			EvaluationHelper.evaluate(context, "mm <- rbindlist(mm, fill=TRUE)");
		} catch (EvaluationException e) {
			log.error(e);
		}
	}

	@Override
	public String getName() {
		return "Merge buffered data frames into single data frame";
	}

}

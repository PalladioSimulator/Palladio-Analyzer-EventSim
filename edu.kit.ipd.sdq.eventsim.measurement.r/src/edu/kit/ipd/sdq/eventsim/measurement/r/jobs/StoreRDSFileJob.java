package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;

/**
 * Saves buffered measurements into an RDS file.
 * 
 * @see https://stat.ethz.ch/R-manual/R-devel/library/base/html/readRDS.html
 * 
 * @author Philipp Merkle
 *
 */
public class StoreRDSFileJob implements RJob {

	private static final Logger log = Logger.getLogger(StoreRDSFileJob.class);
	
	private String rdsFilePath;
	
	public StoreRDSFileJob(String rdsFilePath) {
		this.rdsFilePath = rdsFilePath;
	}
	
	@Override
	public void process(RContext context) {
		log.info("Saving measurements into RDS file. This can take a moment...");
		long start = System.currentTimeMillis();
		try {
			context.getConnection().voidEval("saveRDS(mm, '" + convertToRCompliantPath(rdsFilePath) + "')");
		} catch (RserveException e) {
			log.error("Rserve reported an error while saving measurements to RDS file.", e);
		}
		long end = System.currentTimeMillis();
		context.getStatistics().captureTimeSpentInR(end - start);
		log.info(String.format("Saved measurements into RDS file. Took %.2f seconds.", (end - start) / 1000.0));
		log.info(String.format("RDS file location: %s", rdsFilePath));

	}
	
	private String convertToRCompliantPath(String path) {
		return path.replace("\\", "/");
	}

	@Override
	public String getName() {
		return "Save measurements into RDS file";
	}
	
}

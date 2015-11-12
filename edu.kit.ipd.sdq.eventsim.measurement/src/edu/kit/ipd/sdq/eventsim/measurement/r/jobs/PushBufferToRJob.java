package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.Buffer;
import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;

/**
 * Transfers a {@link Buffer} to R.
 * 
 * @author Philipp Merkle
 *
 */
public class PushBufferToRJob implements RJob {

	private static final Logger log = Logger.getLogger(PushBufferToRJob.class);

	private Buffer buffer;

	private int bufferNumber;

	public PushBufferToRJob(Buffer buffer, int bufferNumber) {
		this.buffer = buffer;
		this.bufferNumber = bufferNumber;
	}

	@Override
	public void process(RContext context) {
		pushBufferToR(context);
	}

	private void pushBufferToR(RContext context) {
		RConnection connection = context.getConnection();

		// first buffer? then initialize list of data frames, one data frame for each buffer
		try {
			if (bufferNumber == 0) {
				connection.voidEval("mm <- list()");
			}
		} catch (RserveException e) {
			log.error(e);
		}

		// create a data frame from this buffer and append to buffer list
		try {
			connection.assign("buffer", createDataFrameFromBuffer(buffer));
			convertCategoricalColumnsToFactorColumns(context);
			connection.voidEval("mm[[length(mm)+1]] <- buffer");
		} catch (RserveException e) {
			log.error(e);
		}
	}

	private void convertCategoricalColumnsToFactorColumns(RContext context) {
		try {
			RConnection connection = context.getConnection();
			connection.voidEval("buffer$what <- as.factor(buffer$what)");
			connection.voidEval("buffer$where.first.type <- as.factor(buffer$where.first.type)");
			connection.voidEval("buffer$where.first.id <- as.factor(buffer$where.first.id)");
			connection.voidEval("buffer$where.second.type <- as.factor(buffer$where.second.type)");
			connection.voidEval("buffer$where.second.id <- as.factor(buffer$where.second.id)");
			connection.voidEval("buffer$where.property <- as.factor(buffer$where.property)");
			connection.voidEval("buffer$who.type <- as.factor(buffer$who.type)");
		} catch (RserveException e) {
			log.error("Rserve reported an error while converting categorical columns to factors", e);
		}
	}

	private REXP createDataFrameFromBuffer(Buffer buffer) {
		try {
			RList rList = new RList(6, true);
			rList.put("what", new REXPString(buffer.getWhat()));
			rList.put("where.first.type", new REXPString(buffer.getWhereFirstType()));
			rList.put("where.first.id", new REXPString(buffer.getWhereFirstId()));
			rList.put("where.second.type", new REXPString(buffer.getWhereSecondType()));
			rList.put("where.second.id", new REXPString(buffer.getWhereSecondId()));
			rList.put("where.property", new REXPString(buffer.getWhereProperty()));
			rList.put("who.type", new REXPString(buffer.getWhoType()));
			rList.put("who.id", new REXPString(buffer.getWhoId()));
			rList.put("value", new REXPDouble(buffer.getValue()));
			rList.put("when", new REXPDouble(buffer.getWhen()));

			for (Entry<String, String[]> context : buffer.getContexts().entrySet()) {
				rList.put(context.getKey(), new REXPString(context.getValue()));
			}
			return REXP.createDataFrame(rList);
		} catch (REXPMismatchException e) {
			// indicates a programming error => throw unchecked
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "Push buffered measurements to R";
	}

}
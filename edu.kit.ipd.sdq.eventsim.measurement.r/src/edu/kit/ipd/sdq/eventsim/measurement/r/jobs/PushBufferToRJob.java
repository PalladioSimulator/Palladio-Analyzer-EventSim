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
import edu.kit.ipd.sdq.eventsim.measurement.r.BufferPart;
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
		log.debug("Pushing measurements buffer " + bufferNumber + " to R.");

		// first buffer? then initialize list of data frames, one data frame for each buffer
		try {
			if (bufferNumber == 0) {
				EvaluationHelper.evaluate(context, "mm <- list()");
			}
		} catch (EvaluationException e) {
			log.error(e);
		}

		// create a data frame from this buffer and append to buffer list
		try {
			connection.assign("buffer", createDataFrameFromBuffer(buffer));
			convertCategoricalColumnsToFactorColumns(context);
			EvaluationHelper.evaluate(context, "mm[[length(mm)+1]] <- buffer");
		} catch (RserveException | EvaluationException e) {
			log.error(e);
		}
	}

	private void convertCategoricalColumnsToFactorColumns(RContext context) {
		try {
			EvaluationHelper.evaluate(context, 
					"buffer$what <- as.factor(buffer$what)",
					"buffer$where.first.type <- as.factor(buffer$where.first.type)",
					"buffer$where.first.id <- as.factor(buffer$where.first.id)",
					"buffer$where.first.name <- as.factor(buffer$where.first.name)",
					"buffer$where.second.type <- as.factor(buffer$where.second.type)",
					"buffer$where.second.id <- as.factor(buffer$where.second.id)",
					"buffer$where.second.name <- as.factor(buffer$where.second.name)",
					"buffer$where.property <- as.factor(buffer$where.property)",
					"buffer$who.type <- as.factor(buffer$who.type)",
					"buffer$who.id <- as.factor(buffer$who.id)",
					"buffer$who.name <- as.factor(buffer$who.name)");
			// next two entries in buffer list are "value" and "when" -- not categorical

			// if there are additional columns for the measurement context, do also convert these into factors
			EvaluationHelper.evaluate(context, "if (length(mm) >= 14) { "
					+ "for (i in 14:length(buffer)) { buffer[[i]] <- as.factor(buffer[[i]]) } }");
		} catch (EvaluationException e) {
			log.error("Rserve reported an error while converting categorical columns to factors", e);
		}
	}

	private REXP createDataFrameFromBuffer(Buffer buffer) {
		try {
			RList rList = new RList(6, true);
			rList.put("what", new REXPString(buffer.getWhat()));
			rList.put("where.first.type", new REXPString(buffer.getWhereFirst().getType()));
			rList.put("where.first.id", new REXPString(buffer.getWhereFirst().getId()));
			rList.put("where.first.name", new REXPString(buffer.getWhereFirst().getName()));
			rList.put("where.second.type", new REXPString(buffer.getWhereSecond().getType()));
			rList.put("where.second.id", new REXPString(buffer.getWhereSecond().getId()));
			rList.put("where.second.name", new REXPString(buffer.getWhereSecond().getName()));
			rList.put("where.property", new REXPString(buffer.getWhereProperty()));
			rList.put("who.type", new REXPString(buffer.getWho().getType()));
			rList.put("who.id", new REXPString(buffer.getWho().getId()));
			rList.put("who.name", new REXPString(buffer.getWho().getName()));
			rList.put("value", new REXPDouble(buffer.getValue()));
			rList.put("when", new REXPDouble(buffer.getWhen()));

			for (Entry<String, BufferPart> context : buffer.getContexts().entrySet()) {
				rList.put(context.getKey() + ".type", new REXPString(context.getValue().type));
				rList.put(context.getKey() + ".id", new REXPString(context.getValue().id));
				rList.put(context.getKey() + ".name", new REXPString(context.getValue().name));
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
package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPWrapper;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.Buffer;
import edu.kit.ipd.sdq.eventsim.measurement.r.Column;
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
				EvaluationHelper.evaluateVoid(context, "mm <- list()");
			}
		} catch (EvaluationException e) {
			log.error(e);
		}

		// create a data frame from this buffer and append to buffer list
		try {
			connection.assign("buffer", createDataFrameFromBuffer(buffer));
			convertCategoricalColumnsToFactorColumns(context);
			EvaluationHelper.evaluateVoid(context, "mm[[length(mm)+1]] <- buffer");
		} catch (RserveException | EvaluationException e) {
			log.error(e);
		}
	}
	
	private void convertCategoricalColumnsToFactorColumns(RContext context) {
		try {
			// first two entries in buffer list are "value" and "when" -- not categorical
			for (Column<?> c : buffer.getColumns()) {
				if (c.isFactorial()) {
					String colName = "buffer$" + c.getName();
					EvaluationHelper.evaluateVoid(context, colName + " <- as.factor(" + colName + ")");
				}
			}
		} catch (EvaluationException e) {
			log.error("Rserve reported an error while converting categorical columns to factors", e);
		}
	}
	
	private REXP createDataFrameFromBuffer(Buffer buffer) {
		try {
			Collection<Column<?>> columns = buffer.getColumns();
			RList rList = new RList(2 + columns.size(), true);
			rList.put("value", new REXPDouble(buffer.getValue()));
			rList.put("when", new REXPDouble(buffer.getWhen()));
			for(Column<?> c : buffer.getColumns()) {
				rList.put(c.getName(), REXPWrapper.wrap(c.values()));
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
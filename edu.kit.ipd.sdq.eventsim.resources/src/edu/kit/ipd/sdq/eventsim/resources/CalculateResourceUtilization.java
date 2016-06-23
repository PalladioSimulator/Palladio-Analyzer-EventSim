package edu.kit.ipd.sdq.eventsim.resources;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.MergeBufferedDataFramesJob;

/**
 * Calculates the {@code UTILIZATION} of active/passive resources, based on the {@code QUEUE_LENGTH}
 * measurements already contained in the measurement data.
 * 
 * @author Philipp Merkle
 *
 */
public class CalculateResourceUtilization implements RJob {

    private static final Logger log = Logger.getLogger(CalculateResourceUtilization.class);

    private static final int WINDOW_SIZE = 10; // in simulation time units

    @Override
    public void process(RContext context) {
        log.info("Calculating resource utilizations...");
        try {
            String rCmd = "window.size = " + WINDOW_SIZE + ";"
                    + "mm[what=='QUEUE_LENGTH', c('duration', 'windownumber') := list(shift(.SD$when, 1, type='lead') - when, ceiling(when / window.size)), by=where.first.id];"

                    // group by window number and calculate sum of busy durations as well as
                    // total duration (busy + idle) per group
                    + "tmp <- mm[what=='QUEUE_LENGTH', .(what='UTILIZATION', busyduration=sum(.SD[value>0]$duration), totalduration=sum(.SD$duration), when=last(.SD$when)), by=.(where.first.id, where.first.name, where.first.type, assemblycontext.id, assemblycontext.name, assemblycontext.type, windownumber)];"

                    // calculate utilization
                    + "tmp[, value := 100 * busyduration / totalduration];"

                    // append calculated rows to original data.table
                    + "mm <- rbindlist(list(mm, tmp), use.names=TRUE, fill=TRUE);"

                    // delete temporary columns
                    + "mm[, c('duration', 'windownumber', 'busyduration', 'totalduration') := NULL];"

                    // delete temp data.table
                    + "rm(tmp)";
            EvaluationHelper.evaluateVoid(context, rCmd);
        } catch (EvaluationException e) {
            log.error(e);
        }
    }

    @Override
    public String getName() {
        return "Calculating resource utilizations based on queue length over time";
    }

}

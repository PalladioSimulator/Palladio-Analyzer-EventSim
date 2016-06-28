package edu.kit.ipd.sdq.eventsim.resources.rjobs;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;

/**
 * Deletes all measurements for the {@code QUEUE_LENGTH} metric, usually after calculating the
 * derived <i>mean queue length over time</i> using {@link CalculateMeanQueueLength}.
 * <p>
 * Raw queue length measurements usually account for half of the total measurements, which is why
 * this job allows to save around 50% main memory and/or storage space.
 * 
 * @author Philipp Merkle
 *
 */
public class DeleteRawQueueLengthMeasurements implements RJob {

    private static final Logger log = Logger.getLogger(DeleteRawQueueLengthMeasurements.class);

    @Override
    public void process(RContext context) {
        try {
            String rCmd = "mm <- mm[what != 'QUEUE_LENGTH']";
            EvaluationHelper.evaluate(context.getConnection(), rCmd);
        } catch (EvaluationException e) {
            log.error(e);
        }
    }

    @Override
    public String getName() {
        return "Delete raw queue length measurements";
    }

}

package edu.kit.ipd.sdq.eventsim.resources.rjobs;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.utils.RHelper;
import edu.kit.ipd.sdq.eventsim.measurement.r.window.TransformToEquidistantObservations;
import edu.kit.ipd.sdq.eventsim.resources.rjobs.window.MeanQueueLengthCalculator;

/**
 * Calculates the mean queue length of resources, based on raw {@code QUEUE_LENGTH} measurements
 * already contained in the measurement data. Compared to raw queue lengths, mean queue lengths have
 * two main advantages: First, they occupy considerably less main memory and/or storage space.
 * Second, they are equidistantly spaced, allowing for calculating statistics like mean, quartiles
 * and quantiles.
 * 
 * @author Philipp Merkle
 *
 */
public class CalculateMeanQueueLength implements RJob {

    private static final Logger log = Logger.getLogger(CalculateMeanQueueLength.class);

    private static final int WINDOW_SIZE = 10; // in simulation time units

    @Override
    public void process(RContext context) {
        try {
            boolean hasAssemblyContext = RHelper.hasColumn(context.getConnection(), "mm", "assemblycontext.id");
            String[] groupByColumns;
            if (hasAssemblyContext) {
                groupByColumns = new String[] { "where.first.id", "assemblycontext.id" };
            } else {
                groupByColumns = new String[] { "where.first.id" };
            }
            TransformToEquidistantObservations transformator = new TransformToEquidistantObservations(context,
                    WINDOW_SIZE);
            transformator.calculateDerivedMetric("QUEUE_LENGTH", "MEAN_QUEUE_LENGTH",
                    new MeanQueueLengthCalculator(WINDOW_SIZE), groupByColumns);
        } catch (EvaluationException e) {
            log.error(e);
        } catch (REXPMismatchException e) {
            log.error(e);
        } catch (RserveException e) {
            log.error(e);
        }
    }

    @Override
    public String getName() {
        return "Calculate mean queue length measurements (equidistantly spaced)";
    }

}

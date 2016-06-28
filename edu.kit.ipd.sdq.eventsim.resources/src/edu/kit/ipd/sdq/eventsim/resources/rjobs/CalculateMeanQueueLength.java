package edu.kit.ipd.sdq.eventsim.resources.rjobs;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;

/**
 * Calculates the mean queue length of resources, based on raw {@code QUEUE_LENGTH} measurements
 * already contained in the measurement data. Compared to raw queue lengths, mean queue lengths have
 * two main advantages: First, they occupy considerably less main memory and/or storage space.
 * Second, they are equidistantly spaced, allowing for calculating statistics like mean, quartiles
 * and quantiles.
 * <p>
 * TODO large parts are copied from {@link CalculateResourceUtilizationEquidistant}, better extract
 * common superclass
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
            // calculate all existing combinations of (where.first.id) x (assemblycontext.id) for
            // QUEUE_LENGTH metric
            REXP combinationsResult = EvaluationHelper.evaluate(context.getConnection(),
                    "mm[what=='QUEUE_LENGTH', .(count=.N), by=.(where.first.id, assemblycontext.id)]");
            RList combinationsResultList = combinationsResult.asList();
            int[] counts = combinationsResultList.at("count").asIntegers();
            String[] whereFirstIds = combinationsResultList.at("where.first.id").asStrings();
            String[] assemblyContextIds = combinationsResultList.at("assemblycontext.id").asStrings();

            // initialize empty list of appropriate size
            EvaluationHelper.evaluate(context.getConnection(),
                    "queuelengths <- vector('list', " + whereFirstIds.length + ")");

            // iterate over all combinations calculated above
            for (int i = 0; i < counts.length; i++) {
                // read all observed states, together with the corresponding time ("when")
                REXP result = EvaluationHelper.evaluate(context.getConnection(),
                        "mm[what=='QUEUE_LENGTH' & where.first.id=='" + whereFirstIds[i] + "', .(when, value)]");
                RList resultList = result.asList();
                double[] when = resultList.at("when").asDoubles();
                int[] states = resultList.at("value").asIntegers();

                // calculate utilization per time frame, called "window"
                double[][] queueLengthPerWindow = calculateMeanQueueLengths(when, states, WINDOW_SIZE);
                int rowsInWindow = queueLengthPerWindow[0].length;
                RList queueLengthsList = new RList(5, true);
                queueLengthsList.put("when", new REXPDouble(queueLengthPerWindow[0]));
                queueLengthsList.put("value", new REXPDouble(queueLengthPerWindow[1]));
                queueLengthsList.put("where.first.id", new REXPString(replicate(whereFirstIds[i], rowsInWindow)));
                queueLengthsList.put("assemblycontext.id",
                        new REXPString(replicate(assemblyContextIds[i], rowsInWindow)));
                queueLengthsList.put("what", new REXPString(replicate("MEAN_QUEUE_LENGTH", rowsInWindow)));

                // create data frame from calculated utilizations and send to R
                context.getConnection().assign("tmp", REXP.createDataFrame(queueLengthsList));
                EvaluationHelper.evaluateVoid(context.getConnection(), "queuelengths[[" + (i + 1) + "]] <- tmp");
            }

            String rCmd = ""
                    // make single data.table from utilizations calculated for the different
                    // combinations
                    + "queuelengths <- rbindlist(queuelengths, fill=TRUE);"

                    // add name and type columns by joining created data.table with a subset
                    // of existing measurements
                    + "queuelengths <- merge(queuelengths, mm[what=='QUEUE_LENGTH', .SD[1], by=.(where.first.id, assemblycontext.id)][,c('when', 'value', 'what', 'where.property') := NULL], by=c('where.first.id', 'assemblycontext.id'));"

                    // integrate created data.table into exiting measurements
                    + "mm <- rbindlist(list(mm, queuelengths), fill=TRUE);"

                    // delete temporary variables
                    + "rm(tmp); rm(queuelengths)";
            EvaluationHelper.evaluate(context.getConnection(), rCmd);
        } catch (EvaluationException e) {
            log.error(e);
        } catch (REXPMismatchException e) {
            log.error(e);
        } catch (RserveException e) {
            log.error(e);
        }
    }

    private String[] replicate(String mp, int size) {
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = mp;
        }
        return result;
    }

    private double[][] calculateMeanQueueLengths(double[] when, int[] states, int windowSize) {
        double whenMax = when[when.length - 1];
        int windowCount = (int) Math.ceil(whenMax / (double) windowSize);

        // sum of queue lengths per window, each multiplied by its duration
        double[] queueLengthSums = new double[windowCount];
        double windowSizeActual = 0;
        int wnd = 0;
        for (int i = 0; i < when.length - 1; i++) {
            int state = states[i];
            double duration = when[i + 1] - when[i]; // how long the state has been observed
            if (windowSizeActual + duration >= windowSize) { // current window full
                double durationOverflow = (windowSizeActual + duration) - windowSize;

                queueLengthSums[wnd] += state * (duration - durationOverflow);

                // overflow duration goes into next window
                queueLengthSums[wnd + 1] = state * durationOverflow;

                wnd++; // start filling next window
                windowSizeActual = durationOverflow;
            } else { // current window not full
                queueLengthSums[wnd] += state * duration;
                windowSizeActual += duration;
            }
        }

        double[] windowEndTimes = new double[windowCount];
        double[] queueLengthMeans = new double[windowCount];
        for (wnd = 0; wnd < windowCount; wnd++) {
            windowEndTimes[wnd] = (wnd + 1) * windowSize;
            queueLengthMeans[wnd] = queueLengthSums[wnd] / windowSize;
        }

        return new double[][] { windowEndTimes, queueLengthMeans };
    }

    @Override
    public String getName() {
        return "Calculate mean queue length measurements (equidistantly spaced)";
    }

}

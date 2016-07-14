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
 * Calculates the {@code UTILIZATION} of active/passive resources over time, based on raw
 * {@code QUEUE_LENGTH} measurements already contained in the measurement data.
 * <p>
 * For a given window size, this job calculates the fraction of busy time vs. window size, i.e. the
 * fraction of (simulation) time the resource has been working actually, expressed in percent.
 * <p>
 * More formally, the utilization u(w) for a window w is calculated as
 * {@code u(w) := 100 * busy_time / total_time}, with total_time being the window size.
 * 
 * @author Philipp Merkle
 * 
 */
public class CalculateResourceUtilizationEquidistant implements RJob {

    private static final Logger log = Logger.getLogger(CalculateResourceUtilizationEquidistant.class);

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
                    "utilizations <- vector('list', " + whereFirstIds.length + ")");

            // iterate over all combinations calculated above
            for (int i = 0; i < counts.length; i++) {
                // read all observed states, together with the corresponding time ("when")
                REXP result = EvaluationHelper.evaluate(context.getConnection(),
                        "mm[what=='QUEUE_LENGTH' & where.first.id=='" + whereFirstIds[i] + "', .(when, value)]");
                RList resultList = result.asList();
                double[] when = resultList.at("when").asDoubles();
                int[] states = resultList.at("value").asIntegers();

                // calculate utilization per time frame, called "window"
                double[][] utilizationPerWindow = calculateUtilization(when, states, WINDOW_SIZE);
                int rowsInWindow = utilizationPerWindow[0].length;
                RList utilizationsList = new RList(5, true);
                utilizationsList.put("when", new REXPDouble(utilizationPerWindow[0]));
                utilizationsList.put("value", new REXPDouble(utilizationPerWindow[1]));
                utilizationsList.put("where.first.id", new REXPString(replicate(whereFirstIds[i], rowsInWindow)));
                utilizationsList.put("assemblycontext.id",
                        new REXPString(replicate(assemblyContextIds[i], rowsInWindow)));
                utilizationsList.put("what", new REXPString(replicate("UTILIZATION", rowsInWindow)));

                // create data frame from calculated utilizations and send to R
                context.getConnection().assign("tmp", REXP.createDataFrame(utilizationsList));
                EvaluationHelper.evaluateVoid(context.getConnection(), "utilizations[[" + (i + 1) + "]] <- tmp");
            }

            String rCmd = ""
                    // make single data.table from utilizations calculated for the different
                    // combinations
                    + "utilizations <- rbindlist(utilizations, fill=TRUE);"

                    // add name and type columns by joining created data.table with a subset
                    // of existing measurements
                    + "utilizations <- merge(utilizations, mm[what=='QUEUE_LENGTH', .SD[1], by=.(where.first.id, assemblycontext.id)][,c('when', 'value', 'what', 'where.property') := NULL], by=c('where.first.id', 'assemblycontext.id'));"

                    // integrate created data.table into exiting measurements
                    + "mm <- rbindlist(list(mm, utilizations), fill=TRUE);"

                    // delete temporary variables
                    + "rm(tmp); rm(utilizations)";
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

    private double[][] calculateUtilization(double[] when, int[] states, int windowDurationMax) {
        double whenMax = when[when.length - 1];
        int windowCount = (int) Math.ceil(whenMax / (double) windowDurationMax);

        double[] idleDurations = new double[windowCount]; // sum of idle duration per window
        double windowDurationActual = 0;
        int wnd = 0;
        for (int i = 0; i < when.length - 1; i++) {
            int state = states[i];
            double duration = when[i + 1] - when[i]; // how long the state has been observed
            double remainingDuration = duration;
            do { // work off remaining duration
                 // if current window will be filled completely
                if (windowDurationActual + remainingDuration >= windowDurationMax) {
                    double windowDurationFree = windowDurationMax - windowDurationActual;
                    double consumedDuration = windowDurationFree;
                    remainingDuration -= consumedDuration;

                    if (state == 0) {
                        idleDurations[wnd] += consumedDuration;
                    }

                    // start filling next window
                    wnd++;
                    windowDurationActual = 0;
                } else { // current window won't be filled completely
                    if (state == 0) {
                        idleDurations[wnd] += remainingDuration;
                    }
                    windowDurationActual += remainingDuration;
                    remainingDuration = 0;
                }
            } while (remainingDuration > 0);
        }

        double[] windowEndTimes = new double[windowCount];
        double[] utilizations = new double[windowCount];
        for (wnd = 0; wnd < windowCount; wnd++) {
            windowEndTimes[wnd] = (wnd + 1) * windowDurationMax;
            utilizations[wnd] = 100 * (windowDurationMax - idleDurations[wnd]) / windowDurationMax;
        }

        return new double[][] { windowEndTimes, utilizations };
    }

    @Override
    public String getName() {
        return "Calculating resource utilizations (equidistantly spaced) based on queue lengths over time";
    }

}

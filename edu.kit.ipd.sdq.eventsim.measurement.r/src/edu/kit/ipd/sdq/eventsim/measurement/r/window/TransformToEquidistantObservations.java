package edu.kit.ipd.sdq.eventsim.measurement.r.window;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;

/**
 * Transforms arbitrarily-spaced observations to equidistantly-spaced observations.
 * 
 * @author Philipp Merkle
 *
 */
public class TransformToEquidistantObservations {

    private double windowSize;

    private RContext context;

    public TransformToEquidistantObservations(RContext context, double windowSize) {
        this.context = context;
        this.windowSize = windowSize;
    }

    public void calculateDerivedMetric(String metric, String derivedMetric, WindowCalculator calculator,
            String... groupByColumns) throws EvaluationException, REXPMismatchException, RserveException {
        // calculate group matrix comprising all combinations (unique rows) of groupByColumns[0] x
        // ... x groupByColumns[n-1] for the given metric
        String[][] groupMatrix = uniqueRows(context.getConnection(), metric, groupByColumns);

        // the number of unique groups in the group matrix
        int groups = groupMatrix[0].length;

        // the number of columns in the group matrix
        int columns = groupByColumns.length;

        // initialize empty list (list-typed vector) of appropriate size for the results
        // (setting initial size improves performance considerably)
        EvaluationHelper.evaluate(context.getConnection(), "result <- vector('list', " + groups + ")");

        // iterate over all groups calculated above
        for (int group = 0; group < groups; group++) {
            // build selection string
            String[] selection = new String[columns + 1];
            selection[0] = "what=='" + metric + "'";
            for (int col = 0; col < columns; col++) {
                if (groupMatrix[col][group] != null) {
                    selection[col + 1] = groupByColumns[col] + "=='" + groupMatrix[col][group] + "'";
                } else {
                    selection[col + 1] = "is.na(" + groupByColumns[col] + ")";
                }

            }
            String selectionString = String.join(" & ", selection);

            // read all observed values, together with the corresponding time ("when")
            REXP result = EvaluationHelper.evaluate(context.getConnection(),
                    "mm[" + selectionString + ", .(when, value)]");
            RList resultList = result.asList();
            double[] when = resultList.at("when").asDoubles();
            int[] values = resultList.at("value").asIntegers();

            // calculate value for current group
            double[][] valuesPerWindow = calculateDerivedMetricForGroup(when, values, windowSize, calculator);
            int valuesInWindow = valuesPerWindow[0].length;
            RList calculatedObservationsList = new RList(3 + columns, true);
            calculatedObservationsList.put("what", new REXPString(replicate(derivedMetric, valuesInWindow)));
            calculatedObservationsList.put("when", new REXPDouble(valuesPerWindow[0]));
            calculatedObservationsList.put("value", new REXPDouble(valuesPerWindow[1]));
            for (int col = 0; col < columns; col++) {
                String columnName = groupByColumns[col];
                calculatedObservationsList.put(columnName,
                        new REXPString(replicate(groupMatrix[col][group], valuesInWindow)));
            }

            // create data frame from calculated equidistant observations and send to R
            context.getConnection().assign("tmp", REXP.createDataFrame(calculatedObservationsList));
            EvaluationHelper.evaluateVoid(context.getConnection(), "result[[" + (group + 1) + "]] <- tmp");
        }

        String rCmd = ""
                // make single data.table from equidistant observations calculated for the different
                // combinations
                + "result <- rbindlist(result, fill=TRUE);"

                // add name and type columns by joining created data.table with a subset
                // of existing measurements
                + "result <- merge(result, mm[what=='" + metric + "', .SD[1], by=.(" + String.join(",", groupByColumns)
                + ")][,c('when', 'value', 'what', 'where.property') := NULL], by=c("
                + String.join(",", quote(groupByColumns)) + "));"

                // integrate created data.table into exiting measurements
                + "mm <- rbindlist(list(mm, result), fill=TRUE);"

                // delete temporary variables
                + "rm(tmp); rm(result)";
        EvaluationHelper.evaluate(context.getConnection(), rCmd);

    }

    private String[][] uniqueRows(RConnection connection, String metric, String[] groupByColumns)
            throws EvaluationException, REXPMismatchException {
        REXP groupsResult = EvaluationHelper.evaluate(connection,
                "unique(mm[what=='" + metric + "', .(" + String.join(",", groupByColumns) + ")])");
        RList groupsList = groupsResult.asList();

        String[][] groupValues = new String[groupsList.size()][];
        for (int i = 0; i < groupsList.size(); i++) {
            String columnName = groupByColumns[i];
            groupValues[i] = groupsList.at(columnName).asStrings();
        }
        return groupValues;
    }

    private String[] quote(String[] strings) {
        String[] quoted = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            quoted[i] = "'" + strings[i] + "'";
        }
        return quoted;
    }

    private String[] replicate(String mp, int size) {
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = mp;
        }
        return result;
    }

    private double[][] calculateDerivedMetricForGroup(double[] when, int[] values, double windowDurationMax,
            WindowCalculator calculator) {
        double whenMax = when[when.length - 1];
        int windowCount = (int) Math.ceil(whenMax / (double) windowDurationMax);

        double[] windows = new double[windowCount];
        double windowDurationActual = 0;
        int wnd = 0;
        for (int i = 0; i < when.length - 1; i++) {
            if (when[i] > when[i + 1]) {
                throw new RuntimeException("Simulation times in measurements needs to be monotonically increasing.");
            }
            // how long the observed value is valid (how long the observation spans)
            double duration = when[i + 1] - when[i];
            double remainingDuration = duration;
            do { // work off remaining duration
                 // if current window will be filled completely
                if (windowDurationActual + remainingDuration >= windowDurationMax) {
                    double windowDurationFree = windowDurationMax - windowDurationActual;
                    double consumedDuration = windowDurationFree;
                    remainingDuration -= consumedDuration;

                    windows[wnd] += calculator.processValue(values[i], consumedDuration);

                    // initialize next window
                    wnd++;
                    windowDurationActual = 0;
                } else { // current window won't be filled completely
                    windows[wnd] += calculator.processValue(values[i], remainingDuration);
                    windowDurationActual += remainingDuration;
                    remainingDuration = 0;
                }
            } while (remainingDuration > 0);
        }

        double[] windowEndTimes = new double[windowCount];
        for (wnd = 0; wnd < windowCount; wnd++) {
            windowEndTimes[wnd] = (wnd + 1) * windowDurationMax;
        }

        return new double[][] { windowEndTimes, windows };
    }

}

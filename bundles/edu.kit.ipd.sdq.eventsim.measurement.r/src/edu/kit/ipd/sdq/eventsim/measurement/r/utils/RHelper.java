package edu.kit.ipd.sdq.eventsim.measurement.r.utils;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.Rserve.RConnection;

import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;

public class RHelper {

    /**
     * 
     * @param context
     * @param variableName
     *            the variable name of a data.frame or data.table
     * @param columnName
     * @return
     */
    public static boolean hasColumn(RConnection connection, String variableName, String columnName) {
        REXP evaluated;
        try {
            String expression = "'" + columnName + "' %in% colnames(" + variableName + ")";
            evaluated = EvaluationHelper.evaluate(connection, expression);
        } catch (EvaluationException e) {
            throw new RuntimeException(e);
        }
        if (evaluated instanceof REXPLogical) {
            boolean[] result = ((REXPLogical) evaluated).isTRUE();
            if (result.length == 1) {
                return result[0];
            } else {
                throw new RuntimeException("Expecting result of size 1, but size is " + result.length);
            }
        } else {
            throw new RuntimeException("Expecting evaluation result of type " + REXPLogical.class + ", but is of type "
                    + evaluated.getClass());
        }
    }
    
}

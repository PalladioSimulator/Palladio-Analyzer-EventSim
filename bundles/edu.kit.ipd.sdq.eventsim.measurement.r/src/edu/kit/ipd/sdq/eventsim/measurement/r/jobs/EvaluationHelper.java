package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;

public class EvaluationHelper {

    public static void evaluateVoid(RContext context, String... commands) throws EvaluationException {
        evaluateVoid(context.getConnection(), commands);
    }

    public static void evaluateVoid(RConnection connection, String... commands) throws EvaluationException {
        for (String command : commands) {
            try {
                connection.voidEval(command);
            } catch (RserveException e) {
                handleRserveException(e, connection, command);
            }
        }
    }

    public static REXP evaluate(RConnection connection, String command) throws EvaluationException {
        try {
            return connection.eval(command);
        } catch (RserveException e) {
            handleRserveException(e, connection, command);
            return null; // won't happen
        }
    }

    private static void handleRserveException(RserveException e, RConnection connection, String command)
            throws EvaluationException {
        // try identifying the cause
        String cause = null;
        try {
            cause = connection.eval("geterrmessage()").asString();
        } catch (REXPMismatchException | RserveException e2) {
            cause = "Error while identifying cause: " + e2.getMessage();
        }
        throw new EvaluationException(String.format("Error for command \"%s\": %s", command, cause));
    }

}

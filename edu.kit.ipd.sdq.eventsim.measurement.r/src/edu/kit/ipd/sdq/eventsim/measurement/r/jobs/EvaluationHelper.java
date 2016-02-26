package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;

public class EvaluationHelper {

	public static void evaluate(RContext context, String... commands) throws EvaluationException {
		for (String command : commands) {
			try {
				context.getConnection().voidEval(command);
			} catch (RserveException e) {
				String cause = "<unavailable>";
				try {
					cause = context.getConnection().eval("geterrmessage()").asString();
				} catch (REXPMismatchException | RserveException e2) {
					e2.printStackTrace();
				}
				throw new EvaluationException(String.format("Error for command \"%s\": %s", command, cause));
			}
		}
	}

}

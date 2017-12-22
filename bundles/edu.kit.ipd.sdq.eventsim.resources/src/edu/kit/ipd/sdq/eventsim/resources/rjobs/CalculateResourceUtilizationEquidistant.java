package edu.kit.ipd.sdq.eventsim.resources.rjobs;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.RContext;
import edu.kit.ipd.sdq.eventsim.measurement.r.RJob;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.utils.RHelper;
import edu.kit.ipd.sdq.eventsim.measurement.r.window.TransformToEquidistantObservations;
import edu.kit.ipd.sdq.eventsim.resources.rjobs.window.UtilizationCalculator;

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
            boolean hasAssemblyContext = RHelper.hasColumn(context.getConnection(), "mm", "assemblycontext.id");
            String[] groupByColumns;
            if (hasAssemblyContext) {
                groupByColumns = new String[] { "where.first.id", "assemblycontext.id" };
            } else {
                groupByColumns = new String[] { "where.first.id" };
            }
            TransformToEquidistantObservations transformator = new TransformToEquidistantObservations(context,
                    WINDOW_SIZE);
            transformator.calculateDerivedMetric("QUEUE_LENGTH", "UTILIZATION", new UtilizationCalculator(WINDOW_SIZE),
                    groupByColumns);
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
        return "Calculating resource utilizations (equidistantly spaced) based on queue lengths over time";
    }

}

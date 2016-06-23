package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class JobExtensionHelper {

    private static final String RJOBS_EXTENSION_POINT_ID = "edu.kit.ipd.sdq.eventsim.measurement.r.rjobs";

    public static List<RJob> createExtensionJobs() {
        IConfigurationElement[] config = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(RJOBS_EXTENSION_POINT_ID);

        List<RJob> jobs = new ArrayList<>();
        for (IConfigurationElement c : config) {
            RJob job;
            try {
                job = (RJob) c.createExecutableExtension("job");
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            jobs.add(job);
        }
        return jobs;
    }

}

package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.usagemodel.Workload;

/**
 * The workload simulation module spawns instances of {@link IUser} according to {@link Workload}
 * specifications.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
public interface IWorkload {

    /**
     * Starts the workload generation.
     * <p>
     * TODO pass respective {@link Workload} instance
     */
    public void generate();

}

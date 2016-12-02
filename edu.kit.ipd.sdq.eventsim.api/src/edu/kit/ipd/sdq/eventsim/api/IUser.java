package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;

/**
 * A user simulates a {@link UsageScenario}. A user is spawned by a workload generator:
 * {@link IWorkload}.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
public interface IUser {

    /**
     * @return the unique identifier of this user
     */
    String getId();

    StackContext getStochasticExpressionContext();

    /**
     * @return the usage scenario representing this user's behaviour
     */
    UsageScenario getUsageScenario();

}

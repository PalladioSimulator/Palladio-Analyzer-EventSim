package edu.kit.ipd.sdq.eventsim.workload.debug;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.interpreter.listener.ITraversalListener;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;

/**
 * This listener prints a debug message whenever an {@link AbstractUserAction} is about to be
 * traversed or when an action has been traversed completely. Call {@code install()} to activate
 * this listener.
 * <p>
 * Notice: This listener does only apply to actions contained in a usage scenario.
 * 
 * @author Philipp Merkle
 * 
 */
public class DebugUsageTraversalListener implements ITraversalListener<AbstractUserAction, User> {

    private static final Logger logger = Logger.getLogger(DebugUsageTraversalListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void before(final AbstractUserAction action, final User who) {
        logger.debug("BEFORE " + PCMEntityHelper.toString(action) + "(User: " + who + ")");
    }

    /**
     * r {@inheritDoc}
     */
    @Override
    public void after(final AbstractUserAction action, final User who) {
        logger.debug("AFTER " + PCMEntityHelper.toString(action) + "(User: " + who + ")");
    }

}

package edu.kit.ipd.sdq.eventsim.workload.interpreter.strategies;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Delay;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.instructions.InterruptTraversal;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.events.ResumeUsageTraversalEvent;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.UsageBehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.workload.interpreter.state.UserState;

/**
 * This traversal strategy is responsible for {@link Delay} actions.
 * 
 * @author Philipp Merkle
 * 
 */
public class DelayTraversalStrategy implements ITraversalStrategy<AbstractUserAction, Delay, User, UserState> {

    @Inject
    private ISimulationModel model;
    
    @Inject // TODO
    private UsageBehaviourInterpreter interpreter;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITraversalInstruction<AbstractUserAction, UserState> traverse(final Delay delay, final User user, final UserState state) {
        // evaluate StoEx
        final PCMRandomVariable delayTimeSpecification = delay.getTimeSpecification_Delay();
        final double delayTime = NumberConverter.toDouble(StackContext.evaluateStatic(delayTimeSpecification.getSpecification()));

        // schedule the traversal to continue after the desired delay
        new ResumeUsageTraversalEvent(model, state, interpreter).schedule(user, delayTime);

        return new InterruptTraversal<>(delay.getSuccessor());
    }

}

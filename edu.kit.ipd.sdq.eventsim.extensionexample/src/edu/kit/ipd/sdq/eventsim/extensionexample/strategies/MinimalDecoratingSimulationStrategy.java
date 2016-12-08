package edu.kit.ipd.sdq.eventsim.extensionexample.strategies;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingSimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

/**
 * Use this class as a template for your own decorating simulation strategy.
 * 
 * @author Philipp Merkle
 *
 */
public class MinimalDecoratingSimulationStrategy
        implements DecoratingSimulationStrategy<AbstractAction, Request> {

    private SimulationStrategy<AbstractAction, Request> decorated;

    @Override
    public void decorate(SimulationStrategy<AbstractAction, Request> decorated) {
        this.decorated = decorated;
    }

    @Override
    public void simulate(AbstractAction action, Request request, Consumer<TraversalInstruction> onFinishCallback) {
        ////////////////////////////////////////////////////////
        // do something *after* decorated simulation strategy //
        ////////////////////////////////////////////////////////

        // delegate simulation to decorated strategy
        decorated.simulate(action, request, traversalInstruction -> {
            ////////////////////////////////////////////////////////
            // do something *after* decorated simulation strategy //
            ////////////////////////////////////////////////////////

            // pass-though traversal instruction returned by decorated strategy
            onFinishCallback.accept(traversalInstruction);
        });
    }

}

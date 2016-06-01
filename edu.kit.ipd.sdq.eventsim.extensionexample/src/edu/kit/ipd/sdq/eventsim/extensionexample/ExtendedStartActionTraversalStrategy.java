package edu.kit.ipd.sdq.eventsim.extensionexample;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.StartAction;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;
import edu.kit.ipd.sdq.eventsim.system.interpreter.strategies.StartActionTraversalStrategy;

public class ExtendedStartActionTraversalStrategy extends StartActionTraversalStrategy {

    @Inject
    ISimulationModel model;

    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(StartAction action, Request request,
            RequestState state) { 
        ExtendedRequest ourRequest = (ExtendedRequest) request;
        int counter = ourRequest.getCounter();

        // before traverse
        System.out.println("ExtendedRequest #" + counter + " is about to traverse " + action + " @ "
                + model.getSimulationControl().getCurrentSimulationTime());

        // delegate actual traverse to super class
        ITraversalInstruction<AbstractAction, RequestState> instruction = super.traverse(action, request, state);

        // after traverse
        System.out.println("ExtendedRequest #" + counter + " finished traversal of " + action);

        return instruction;
    }

}

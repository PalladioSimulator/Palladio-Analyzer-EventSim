package edu.kit.ipd.sdq.eventsim.extensionexample.strategies;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.StartAction;

import com.google.inject.Inject;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.extensionexample.entites.ExtendedRequest;
import edu.kit.ipd.sdq.eventsim.extensionexample.launch.ConfigurationConstants;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingTraversalStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.ITraversalInstruction;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;

public class ExtendedStartActionTraversalStrategy
        extends DecoratingTraversalStrategy<AbstractAction, StartAction, Request, RequestState> {

    @Inject
    private ISimulationModel model;

    @Inject
    private ISimulationConfiguration configuration;

    private String prefix;

    @Override
    public ITraversalInstruction<AbstractAction, RequestState> traverse(StartAction action, Request request,
            RequestState state) {
        if (prefix == null) {
            prefix = loadCustomPrefixFromConfiguration(configuration);
        }

        ExtendedRequest ourRequest = (ExtendedRequest) request;
        int counter = ourRequest.getCounter();

        // before traverse
        System.out.println(prefix + "ExtendedRequest #" + counter + " is about to traverse " + action + " @ "
                + model.getSimulationControl().getCurrentSimulationTime());

        // delegate actual traverse to decorated class
        ITraversalInstruction<AbstractAction, RequestState> instruction = traverseDecorated(action, request, state);

        // after traverse
        System.out.println(prefix + "ExtendedRequest #" + counter + " finished traversal of " + action);

        return instruction;
    }

    private String loadCustomPrefixFromConfiguration(ISimulationConfiguration configuration) {
        if (configuration.getConfigurationMap().get(ConfigurationConstants.CONSOLE_PREFIX) != null) {
            return (String) configuration.getConfigurationMap().get(ConfigurationConstants.CONSOLE_PREFIX);
        }
        return "";
    }

}

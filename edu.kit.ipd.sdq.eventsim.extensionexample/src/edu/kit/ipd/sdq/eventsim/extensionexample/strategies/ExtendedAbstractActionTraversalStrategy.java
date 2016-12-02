package edu.kit.ipd.sdq.eventsim.extensionexample.strategies;

import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.seff.AbstractAction;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.extensionexample.entites.ExtendedRequest;
import edu.kit.ipd.sdq.eventsim.extensionexample.launch.ConfigurationConstants;
import edu.kit.ipd.sdq.eventsim.interpreter.DecoratingSimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

public class ExtendedAbstractActionTraversalStrategy implements DecoratingSimulationStrategy<AbstractAction, Request> {

    private static final Logger logger = Logger.getLogger(ExtendedAbstractActionTraversalStrategy.class);

    @Inject
    private ISimulationConfiguration configuration;

    @Inject
    private ISimulationMiddleware middleware;

    private String prefix;

    private SimulationStrategy<AbstractAction, Request> decorated;

    @Override
    public void decorate(SimulationStrategy<AbstractAction, Request> decorated) {
        this.decorated = decorated;
    }

    @Override
    public void simulate(AbstractAction action, Request request, Consumer<Procedure> onFinishCallback) {
        // initialize from configuration
        if (prefix == null) {
            prefix = loadCustomPrefixFromConfiguration(configuration);
        }

        // 1) before simulating decorated action, log info message
        ExtendedRequest ourRequest = (ExtendedRequest) request;
        int counter = ourRequest.getCounter();
        logger.info(String.format("[t=%s] %s ExtendedRequest #%s starts traversal of %s",
                middleware.getSimulationControl().getCurrentSimulationTime(), prefix, counter, action));

        // 2) delay the request
        ourRequest.delay(3.33, () -> {
            logger.info(String.format("[t=%s] %s ExtendedRequest #%s has been delayed successfully",
                    middleware.getSimulationControl().getCurrentSimulationTime(), prefix, counter));
            // 3) then simulate decorated action
            decorated.simulate(action, request, instruction -> {
                // 4) then, after simulating decorated action, log another info message
                logger.info(String.format("[t=%s] %s ExtendedRequest #%s finished traversal of %s",
                        middleware.getSimulationControl().getCurrentSimulationTime(), prefix, counter, action));

                // 4) pass through the instruction returned by decorated strategy, because we want
                // the log message above to printed *before* the simulation continues with
                // simulating the next action
                onFinishCallback.accept(instruction);
            });
        });

    }

    private String loadCustomPrefixFromConfiguration(ISimulationConfiguration configuration) {
        if (configuration.getConfigurationMap().get(ConfigurationConstants.CONSOLE_PREFIX) != null) {
            return (String) configuration.getConfigurationMap().get(ConfigurationConstants.CONSOLE_PREFIX);
        }
        return "";
    }

}

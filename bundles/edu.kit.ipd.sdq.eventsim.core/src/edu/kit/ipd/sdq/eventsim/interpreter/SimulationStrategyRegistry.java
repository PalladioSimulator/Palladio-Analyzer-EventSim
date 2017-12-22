package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.seff.AbstractAction;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModule;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModuleRegistry;
import edu.kit.ipd.sdq.eventsim.modules.SimulationStrategyEntry;

/**
 * 
 * @author Philipp Merkle
 *
 */
@Singleton
public class SimulationStrategyRegistry<A extends Entity, E extends EventSimEntity> {

    private static final Logger logger = Logger.getLogger(SimulationStrategyRegistry.class);

    private final Map<Class<?>, SimulationStrategy<A, E>> handlerMap = new HashMap<>();

    @Inject
    public SimulationStrategyRegistry(Injector injector, SimulationModuleRegistry moduleRegistry) {
        for (SimulationModule m : moduleRegistry.getModules()) {
            // skip, if module is disabled
            if (!m.isEnabled()) {
                continue;
            }
            for (SimulationStrategyEntry s : m.getSimulationStrategies()) {
                try {
                    Class<?> actionType = Class.forName(s.getActionType());
                    SimulationStrategy<A, E> strategy = (SimulationStrategy<A, E>) s.getStrategy();
                    registerActionHandler(actionType, strategy);
                    injector.injectMembers(strategy);
                } catch (ClassNotFoundException e) {
                    logger.error(e);
                } catch (InvalidRegistryObjectException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Adds a handler for the specified action class. If a handler for the specified action class
     * already exists, the existing handler will be decorated by the specified handler.
     * 
     * @param actionClass
     *            the action class
     * @param handler
     *            the handler that is to be registered
     */
    public void registerActionHandler(final Class<?> actionClass, final SimulationStrategy<A, E> handler) {
        SimulationStrategy<A, E> strategy = handler;
        if (handlerMap.containsKey(actionClass)) {
            SimulationStrategy<A, E> existingStrategy = handlerMap.get(actionClass);
            DecoratingSimulationStrategy<A, E> decoratingStrategy = null;

            // check if the strategy to be registered brings decoration capabilities
            for (Class<?> iface : strategy.getClass().getInterfaces()) {
                if (iface.equals(DecoratingSimulationStrategy.class)) {
                    decoratingStrategy = (DecoratingSimulationStrategy<A, E>) strategy;
                }
            }

            // decorate existing strategy
            decoratingStrategy.decorate(existingStrategy);
            logger.info(String.format("Decorating simulation strategy: %s decorates %s", decoratingStrategy,
                    existingStrategy));

            strategy = decoratingStrategy;
        }
        handlerMap.put(actionClass, strategy);
    }

    /**
     * Removes the handler for the specified action class.
     * 
     * @param actionClass
     *            the action class whose handler is to be unregistered
     */
    public void unregisterActionHandler(final Class<? extends AbstractAction> actionClass) {
        if (!handlerMap.containsKey(actionClass)) {
            logger.warn("Tried to unregister the simulationn strategy for " + actionClass.getName()
                    + ", but no handler has been registered for this action.");
        }
        handlerMap.remove(actionClass);
    }

    public SimulationStrategy<A, E> lookup(Class<? extends A> type) {
        return handlerMap.get(type);
    }

}

package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.seff.AbstractAction;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.interpreter.state.AbstractInterpreterState;

/**
 * 
 * @author Philipp Merkle
 * 
 * @param <A>
 *            the least common parent type of all actions that are to be traversed
 */
@Singleton
public class TraversalStrategyRegistry<A extends Entity> {

    private static final String INTERPRETER_STRATEGY_EXTENSION_POINT_ID = "edu.kit.ipd.sdq.eventsim.interpreter.strategy";

    private static final Logger logger = Logger.getLogger(TraversalStrategyRegistry.class);

    private final Map<Class<? extends A>, ITraversalStrategy<A, ? extends A, ?, AbstractInterpreterState<A>>> handlerMap = new HashMap<>();

    @Inject
    public TraversalStrategyRegistry(Injector injector) {
        IExtensionRegistry er = Platform.getExtensionRegistry();
        IExtensionPoint ep = er.getExtensionPoint(INTERPRETER_STRATEGY_EXTENSION_POINT_ID);
        for (IExtension extension : ep.getExtensions()) {
            for (IConfigurationElement config : extension.getConfigurationElements()) {
                try {
                    Class<? extends A> actionType = (Class<? extends A>) Class.forName(config.getAttribute("action"));
                    ITraversalStrategy<A, ? extends A, ?, AbstractInterpreterState<A>> strategy = (ITraversalStrategy<A, ? extends A, ?, AbstractInterpreterState<A>>) config.createExecutableExtension("strategy");
                    registerActionHandler(actionType, strategy);
                    injector.injectMembers(strategy);
                } catch (ClassNotFoundException e) {
                    logger.error(e);
                } catch (InvalidRegistryObjectException e) {
                    logger.error(e);
                } catch (CoreException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Adds a handler for the specified action class, which must be a subtype of
     * {@link AbstractAction}.
     * 
     * @param actionClass
     *            the action class
     * @param handler
     *            the handler that is to be registered
     */
    public void registerActionHandler(final Class<? extends A> actionClass,
            final ITraversalStrategy<A, ? extends A, ?, AbstractInterpreterState<A>> handler) {
        // TODO
        // assert (UsagemodelPackage.eINSTANCE.getAbstractUserAction().isSuperTypeOf(
        // actionClass)) : "The parameter \"action\" has to be a subtype of AbstractUserAction, but
        // was "
        // + actionClass.getName();
        // if (handlerMap.containsKey(actionClass)) {
        // if (logger.isEnabledFor(Level.WARN))
        // logger.warn("Registered a handler for " + actionClass.getName()
        // + ", for which a handler was already registered. The former handler has been
        // overwritten.");
        // }
        handlerMap.put(actionClass, handler);
    }

    /**
     * Removes the handler for the specified action class.
     * 
     * @param actionClass
     *            the action class whose handler is to be unregistered
     */
    public void unregisterActionHandler(final Class<? extends AbstractAction> actionClass) {
        // TODO
        // assert (UsagemodelPackage.eINSTANCE.getAbstractUserAction().isSuperTypeOf(
        // actionClass)) : "The parameter \"action\" has to be a subtype of AbstractUserAction, but
        // was "
        // + actionClass.getName();
        // if (handlerMap.containsKey(actionClass)) {
        // if (logger.isEnabledFor(Level.WARN))
        // logger.warn("Tried to unregister the action handler of " + actionClass.getName()
        // + ", but no handler has been registered for this action.");
        // }
        handlerMap.remove(actionClass);
    }

    public ITraversalStrategy<A, ? extends A, ?, ? extends AbstractInterpreterState<A>> lookup(Class<? extends A> type) {
        return handlerMap.get(type);
    }

}

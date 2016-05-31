package edu.kit.ipd.sdq.eventsim.system.interpreter;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ForkedBehaviour;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.events.SystemRequestSpawnEvent;
import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.action.FindActionInBehaviour;
import edu.kit.ipd.sdq.eventsim.interpreter.BehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.system.entities.ForkedRequest;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.ForkedRequestState;
import edu.kit.ipd.sdq.eventsim.system.interpreter.state.RequestState;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

/**
 * An interpreter for {@link ResourceDemandingSEFF}s.
 * 
 * @author Philipp Merkle
 * 
 * @see BehaviourInterpreter
 */
@Singleton
public class SeffBehaviourInterpreter extends BehaviourInterpreter<AbstractAction, Request, RequestState> {

    private static final Logger logger = Logger.getLogger(SeffBehaviourInterpreter.class);

    @Inject
    private PCMModelCommandExecutor executor;
    
    @Inject
    private ISimulationMiddleware middleware;
    
    @Inject
    private TraversalListenerRegistry<AbstractAction, Request, RequestState> listenerRegistry;

    /**
     * Starts the traversal of the {@link ResourceDemandingSEFF} associated with the specified
     * component and signature.
     */
	public void beginTraversal(Request request, final ComponentInstance component, final OperationSignature signature,
			final StackContext stoExContext) {
        request.notifyEnteredSystem();

        // initialize traversal state and StoEx context
        RequestState state = new RequestState(stoExContext);
        state.pushStackFrame();
        state.setComponent(component);
        request.setRequestState(state);
        
        // fire request start event
        middleware.triggerEvent(new SystemRequestSpawnEvent(request));

        // find start action
        final ResourceDemandingSEFF seff = component.getServiceEffectSpecification(signature);
        final StartAction startAction = executor.execute(new FindActionInBehaviour<StartAction>(seff, StartAction.class));

        // begin traversal
        state.setCurrentPosition(startAction);

        if(logger.isDebugEnabled())
        	logger.debug("Begin interpreting SEFF " + seff.getId() + " (AssemblyContext: "
                + PCMEntityHelper.toString(component.getAssemblyCtx()) + ", OperationSignature: "
                + PCMEntityHelper.toString(signature));
        this.traverse(request, startAction, state);
    }

    public void beginTraversal(ForkedRequest request, ForkedBehaviour behaviour, final RequestState parentState) {
        request.notifyEnteredSystem();

        // initialise traversal state and StoEx context
        StackContext stoExContext = new StackContext();
        stoExContext.getStack().pushStackFrame(parentState.getStoExContext().getStack().currentStackFrame().copyFrame());
        ForkedRequestState state = new ForkedRequestState(parentState, stoExContext);
        state.pushStackFrame();
        state.setComponent(parentState.getComponent());
        request.setRequestState(state);

        // find start action
        final StartAction startAction = executor.execute(new FindActionInBehaviour<StartAction>(behaviour, StartAction.class));

        // begin traversal
        state.setCurrentPosition(startAction);
        this.traverse(request, startAction, state);
    }

    public void resumeTraversal(Request request, final RequestState state) {
        // find next action and resume traversal
        super.traverse(request, state.getCurrentPosition(), state);
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public ITraversalStrategy<AbstractAction, ? extends AbstractAction, Request, RequestState> loadTraversalStrategy(AbstractAction action) {
//        return strategyRegistry.get().lookup(action.eClass().getInstanceClass());
//    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAfterListener(final AbstractAction action, final Request request, RequestState state) {
        listenerRegistry.notifyAfterListener(action, request, state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyBeforeListener(final AbstractAction action, final Request request, RequestState state) {
        listenerRegistry.notifyBeforeListener(action, request, state);
    }

}

package edu.kit.ipd.sdq.eventsim.system.command;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.ICommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.IPCMCommand;
import edu.kit.ipd.sdq.eventsim.command.action.FindActionsInSeff;
import edu.kit.ipd.sdq.eventsim.command.action.FindSeffsForAssemblyContext;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.listener.AbstractExternalCallListener;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;
import edu.kit.ipd.sdq.eventsim.util.ParameterHelper;

/**
 * This command sets up the handling of PCM parameter characterisations for
 * {@link ExternalCallAction}s. ExternalCallActions accept input parameters and can return output
 * parameters, both of which are realised by modifying the current stack frame (see:
 * {@link StackContext}). These modifications are performed by this command, once installed.
 * 
 * @author Philipp Merkle
 * 
 */
public class InstallExternalCallParameterHandling implements IPCMCommand<Void> {

    private static final Logger logger = Logger.getLogger(InstallExternalCallParameterHandling.class);
    private static final boolean debug = logger.isDebugEnabled();

    private TraversalListenerRegistry<AbstractAction, Request> traversalListeners;

    public InstallExternalCallParameterHandling(TraversalListenerRegistry<AbstractAction, Request> traversalListeners) {
        this.traversalListeners = traversalListeners;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void execute(final PCMModel pcm, final ICommandExecutor<PCMModel> executor) {
        // find all external calls
        final Set<ExternalCallAction> externalCalls = new LinkedHashSet<ExternalCallAction>();
        for (final AssemblyContext assemblyCtx : pcm.getSystemModel().getAssemblyContexts__ComposedStructure()) {
            final List<ResourceDemandingSEFF> seffs = executor.execute(new FindSeffsForAssemblyContext(assemblyCtx));
            for (ResourceDemandingSEFF s : seffs) {
                externalCalls.addAll(executor.execute(new FindActionsInSeff<>(s, ExternalCallAction.class)));
            }
        }
        if (externalCalls != null) {
            for (final ExternalCallAction c : externalCalls) {
                traversalListeners.addTraversalListener(c, new ExternalCallTraversalListener());
            }
        }
        // the listeners are mounted; we don't need to return anything.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cachable() {
        return false;
    }

    private static final class ExternalCallTraversalListener extends AbstractExternalCallListener {

        @Override
        public void before(final ExternalCallAction call, final Request request,
                final ComponentInstance callingComponent) {
            if (debug) {
                logger.debug("Begin handling external call input parameters");
            }

            final StackContext ctx = request.getRequestState().getStoExContext();

            // find the component which provides the required call
            final OperationSignature calledService = call.getCalledService_ExternalService();
            final ComponentInstance providingComponent = callingComponent.getProvidingComponent(calledService);

            // get a reference on the current stack frame which is being covered soon
            final SimulatedStackframe<Object> outerFrame = ctx.getStack().currentStackFrame();

            // enter a new scope in which the call is being executed
            final SimulatedStackframe<Object> serviceBodyFrame = ctx.getStack().createAndPushNewStackFrame();

            // add component parameters
            serviceBodyFrame.addVariables(providingComponent.getComponentParameters());

            // evaluate the input parameters and add them to the call's scope
            final List<VariableUsage> parameters = call.getInputVariableUsages__CallAction();
            ParameterHelper.evaluateParametersAndCopyToFrame(parameters, outerFrame, serviceBodyFrame);

            if (debug) {
                logger.debug("Finished handling external call input parameters");
            }
        }

        @Override
        public void after(final ExternalCallAction call, final Request request,
                final ComponentInstance callingComponent) {
            if (debug) {
                logger.debug("Begin handling external call output parameters");
            }

            final StackContext ctx = request.getRequestState().getStoExContext();

            // get a reference on the current stack frame which is being removed soon
            final SimulatedStackframe<Object> serviceBodyFrame = ctx.getStack().currentStackFrame();

            // remove the current stack frame. This restores the pre-call scope.
            ctx.getStack().removeStackFrame();

            // evaluate the return parameters of the call and add them to the current scope
            final List<VariableUsage> parameters = call.getReturnVariableUsage__CallReturnAction();
            final SimulatedStackframe<Object> currentFrame = ctx.getStack().currentStackFrame();
            ParameterHelper.evaluateParametersAndCopyToFrame(parameters, serviceBodyFrame, currentFrame);

            if (debug) {
                logger.debug("Finished handling external call output parameters");
            }
        }

    }

}

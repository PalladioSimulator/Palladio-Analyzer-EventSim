package edu.kit.ipd.sdq.eventsim.system.events;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.interpreter.SeffBehaviourInterpreter;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;

/**
 * Schedule this event to begin the traversal of a {@link ResourceDemandingSEFF} (RD-SEFF).
 * <p>
 * The {@link Request} that is supposed to traverse the SEFF, is passed to the {@code schedule()}
 * method.
 * 
 * @author Philipp Merkle
 * 
 */
public class BeginSeffTraversalEvent extends AbstractSimEventDelegator<Request> {

    private final ComponentInstance component;
    private final OperationSignature signature;
    private StackContext stoExContext;
    private SeffBehaviourInterpreter interpreter;

    /**
     * Use this constructor to begin the traversal of the RD-SEFF provided by the specified {@code
     * component}. The {@code signature} specifies which SEFF is to be used, as a component may
     * contain a SEFF for each provided signature.
     * 
     * @param model
     *            the model
     * @param component
     *            the component providing the SEFF
     * @param signature
     *            the signature whose SEFF is to be traversed
     * @param parentState
     *            the state of the usage traversal
     */
    public BeginSeffTraversalEvent(final ISimulationModel model, final ComponentInstance component,
            final OperationSignature signature, StackContext stoExContext, SeffBehaviourInterpreter interpreter) {
        super(model, "BeginUsageTraversalEvent");
        this.component = component;
        this.signature = signature;
        this.stoExContext = stoExContext;
        this.interpreter = interpreter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventRoutine(final Request who) {
        interpreter.beginTraversal(who, this.component, this.signature, stoExContext);
    }

}

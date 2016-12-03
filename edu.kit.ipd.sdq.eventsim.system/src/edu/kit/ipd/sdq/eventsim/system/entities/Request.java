package edu.kit.ipd.sdq.eventsim.system.entities;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.api.events.SystemRequestFinishedEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SystemRequestSpawnEvent;
import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.action.FindActionInBehaviour;
import edu.kit.ipd.sdq.eventsim.debug.DebugEntityListener;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.TraversalException;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategyRegistry;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.interpreter.state.EntityState;
import edu.kit.ipd.sdq.eventsim.system.staticstructure.ComponentInstance;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

/**
 * This entity represents the execution of a system call, which has been issued by a {@link User}.
 * 
 * @author Philipp Merkle
 * 
 * @see EventSimEntity
 */
public class Request extends EventSimEntity implements IRequest {

    public static final String COMPONENT_PROPERTY = "componentInstance";

    private static final Logger logger = Logger.getLogger(Request.class);

    /** the user that has issued the request */
    private final IUser user;

    /** the system call that is to be simulated by this request */
    private final EntryLevelSystemCall call;

    private EntityState<AbstractAction> state;

    @Inject
    private PCMModelCommandExecutor executor;

    @Inject
    private TraversalListenerRegistry<AbstractAction, Request> listenerRegistry;

    @Inject
    private Provider<SimulationStrategyRegistry<AbstractAction, Request>> strategyRegistry;

    @Inject
    private ISimulationMiddleware middleware;

    /**
     * Constructs a new Request representing the execution of the specified system call, which has
     * been issued by the given User.
     * 
     * @param model
     *            the simulation model
     * @param call
     *            the system call
     * @param user
     *            the User that has issued the Request
     */
    @Inject
    public Request(final ISimulationModel model, @Assisted final EntryLevelSystemCall call,
            @Assisted final IUser user) {
        super(model, "Request");
        this.call = call;
        this.user = user;

        initState();

        // install debug listener, if debugging is enabled
        if (logger.isDebugEnabled()) {
            this.addEntityListener(new DebugEntityListener(this));
        }
    }

    private void initState() {
        // initialise traversal state and StoEx context
        state = new EntityState<>(user.getStochasticExpressionContext());
    }

    /**
     * Returns the user that has issued this Request.
     * 
     * @return the user
     */
    public IUser getUser() {
        return this.user;
    }

    /**
     * Returns the system call that is to be executed by this Request.
     * 
     * @return the system call
     */
    public EntryLevelSystemCall getSystemCall() {
        return this.call;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Request#" + this.getEntityId() + " of " + this.getUser().getId();
    }

    @Override
    public Request getParent() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getName();
    }

    public EntityState<AbstractAction> getRequestState() {
        return state;
    }

    public void setRequestState(EntityState<AbstractAction> state) {
        this.state = state;
    }

    @Override
    public long getId() {
        return getEntityId();
    }

    @Override
    public AbstractAction getCurrentPosition() {
        return state.getCurrentPosition();
    }

    public ComponentInstance getCurrentComponent() {
        return state.getProperty(COMPONENT_PROPERTY, ComponentInstance.class);
    }

    public void enterBehaviour(ResourceDemandingBehaviour behaviour, Procedure onFinishCallback) {
        state.pushStackFrame();
        state.setOnFinishCallback(onFinishCallback);
    }

    public void leaveBehaviour() {
        // TODO make sure there is an open state left

        AbstractAction finishedAction = state.getCurrentPosition();
        notifyAfterAction(finishedAction, this);

        if (state.size() == 1) {
            notifyLeftSystem();
        }

        Procedure callback = state.getOnFinishCallback();
        state.popStackFrame();
        callback.execute();
    }

    // TODO pull up
    public void delay(double waitingTime, Procedure onResumeCallback) {
        new AbstractSimEventDelegator<Request>(getModel(), "waitEvent") {
            @Override
            public void eventRoutine(Request who) {
                onResumeCallback.execute();
            }
        }.schedule(this, waitingTime);
    }

    /**
     * Simulates the given {@code behaviour} by simulating the nested chain of actions. If these
     * actions contain further {@link ScenarioBehaviour}s, these are simulated as well (and so on,
     * recursively).
     * <p>
     * Once the entire behaviour has been simulated, the given {@code callback} will be invoked.
     * <p>
     * When this method returns, there is no guarantee whether the behaviour has been simulated or
     * not.
     * 
     * @param behaviour
     *            the behaviour to be simulated
     * @param onCompletionCallback
     *            will be invoked once the behaviour has been simulated completely
     */
    public void simulateBehaviour(ResourceDemandingBehaviour behaviour, ComponentInstance component,
            Procedure onCompletionCallback) {
        // find start action
        final StartAction start = executor
                .execute(new FindActionInBehaviour<StartAction>(behaviour, StartAction.class));

        enterBehaviour(behaviour, onCompletionCallback);
        state.addProperty(COMPONENT_PROPERTY, component);

        if (state.size() == 1) {
            notifyEnteredSystem();
        }

        simulateAction(start);
    }

    // public void simulateBehaviour(ResourceDemandingBehaviour behaviour, Procedure
    // onCompletionCallback) {
    // ComponentInstance component = state.getProperty(COMPONENT_PROPERTY, ComponentInstance.class);
    // simulateBehaviour(behaviour, component, onCompletionCallback);
    // }

    // TODO pull up
    public void simulateAction(AbstractAction action) {
        if (state.getCurrentPosition() != null) {
            AbstractAction finishedAction = state.getCurrentPosition();
            notifyAfterAction(finishedAction, this);
        }

        state.setCurrentPosition(action);

        final SimulationStrategy<AbstractAction, Request> simulationStrategy = strategyRegistry.get()
                .lookup((Class<? extends AbstractAction>) action.eClass().getInstanceClass());
        if (simulationStrategy == null) {
            throw new TraversalException(
                    "No traversal strategy could be found for " + PCMEntityHelper.toString(action));
        }

        notifyBeforeAction(action, this);

        logger.debug(String.format("%s simulating %s @ %s", this.toString(), PCMEntityHelper.toString(action),
                getModel().getSimulationControl().getCurrentSimulationTime()));

        // 1) tell simulation strategy to simulate the action's effects on this request
        simulationStrategy.simulate(action, this, instruction -> {
            // 2) then, execute traversal instruction returned by simulation strategy
            instruction.execute();
        });
    }

    private void notifyAfterAction(final AbstractAction action, final Request request) {
        listenerRegistry.notifyAfterListener(action, request);
    }

    private void notifyBeforeAction(final AbstractAction action, final Request request) {
        listenerRegistry.notifyBeforeListener(action, request);
    }

    @Override
    public void notifyEnteredSystem() {
        super.notifyEnteredSystem();
        middleware.triggerEvent(new SystemRequestSpawnEvent(this));
    }

    @Override
    public void notifyLeftSystem() {
        super.notifyLeftSystem();
        middleware.triggerEvent(new SystemRequestFinishedEvent(this));
    }

}

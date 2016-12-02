package edu.kit.ipd.sdq.eventsim.workload.entities;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.useraction.FindActionInUsageBehaviour;
import edu.kit.ipd.sdq.eventsim.debug.DebugEntityListener;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.TraversalException;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategy;
import edu.kit.ipd.sdq.eventsim.interpreter.SimulationStrategyRegistry;
import edu.kit.ipd.sdq.eventsim.interpreter.TraversalListenerRegistry;
import edu.kit.ipd.sdq.eventsim.interpreter.state.EntityState;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;
import edu.kit.ipd.sdq.eventsim.workload.generator.IWorkloadGenerator;

/**
 * This entity represents a user of the system under simulation. Users issue system calls while
 * traversing a usage scenario. They are spawned by a {@link IWorkloadGenerator}.
 * 
 * @author Philipp Merkle
 * @author Christoph Föhrdes
 * 
 * @see EventSimEntity
 */
public class User extends EventSimEntity implements IUser {

    private static final Logger logger = Logger.getLogger(User.class);

    /** the usage scenario to be simulated by this User */
    private final UsageScenario scenario;

    private EntityState<AbstractUserAction> state;

    @Inject
    private PCMModelCommandExecutor executor;

    @Inject
    private TraversalListenerRegistry<AbstractUserAction, User> listenerRegistry;

    @Inject
    private Provider<SimulationStrategyRegistry<AbstractUserAction, User>> strategyRegistry;

    /**
     * Constructs a new User that is supposed to traverse the specified usage scenario.
     * 
     * @param model
     *            the simulation model
     * @param scenario
     *            the usage scenario
     */
    @Inject
    public User(final ISimulationModel model, @Assisted final UsageScenario scenario) {
        super(model, "User");
        this.scenario = scenario;

        initState();

        if (logger.isDebugEnabled()) {
            this.addEntityListener(new DebugEntityListener(this));
        }
    }

    private void initState() {
        // initialise traversal state and StoEx context
        state = new EntityState<>(new StackContext());
        state.getStoExContext().getStack().createAndPushNewStackFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "IUser" + this.getEntityId();
    }

    /**
     * Returns the usage scenario that is to be executed by this User.
     * 
     * @return the usage scenario
     */
    @Override
    public UsageScenario getUsageScenario() {
        return this.scenario;
    }

    public EntityState<AbstractUserAction> getUserState() {
        return state;
    }

    @Override
    public StackContext getStochasticExpressionContext() {
        return state.getStoExContext();
    }

    public void enterScenarioBehaviour(ScenarioBehaviour behaviour, Procedure onFinishCallback) {
        state.pushStackFrame();
        state.setOnFinishCallback(onFinishCallback);
    }

    public void leaveScenarioBehaviour() {
        if (state.isEmpty()) {
            throw new EventSimException("Tried to leave scenario behaviour, but there is no open scope.");
        }

        AbstractUserAction finishedAction = state.getCurrentPosition();
        notifyAfterAction(finishedAction, this);

        if (state.size() == 1) {
            notifyLeftSystem();
        }
        
        Procedure callback = state.getOnFinishCallback();
        state.popStackFrame();
        callback.execute();
    }

    public void delay(double waitingTime, Procedure onResumeCallback) {
        new AbstractSimEventDelegator<User>(getModel(), "waitEvent") {
            @Override
            public void eventRoutine(User who) {
                onResumeCallback.execute();
            }
        }.schedule(this, waitingTime);
    }

    public void simulateAction(AbstractUserAction action) {
        if (state.getCurrentPosition() != null) {
            AbstractUserAction finishedAction = state.getCurrentPosition();
            notifyAfterAction(finishedAction, this);
        }

        state.setCurrentPosition(action);

        final SimulationStrategy<AbstractUserAction, User> simulationStrategy = strategyRegistry.get()
                .lookup((Class<? extends AbstractUserAction>) action.eClass().getInstanceClass());
        if (simulationStrategy == null) {
            throw new TraversalException(
                    "No simulation strategy could be found for " + PCMEntityHelper.toString(action));
        }

        notifyBeforeAction(action, this);

        logger.debug(String.format("%s simulating %s @ %s", this.toString(), PCMEntityHelper.toString(action),
                getModel().getSimulationControl().getCurrentSimulationTime()));

        // 1) tell simulation strategy to simulate the action's effects on this user
        simulationStrategy.simulate(action, this, instruction -> {
            // 2) then, execute traversal instruction returned by simulation strategy
            instruction.execute();
        });
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
    public void simulateBehaviour(ScenarioBehaviour behaviour, Procedure onCompletionCallback) {
        // outermost behaviour?
        if (PCMEntityHelper.equals(behaviour, scenario.getScenarioBehaviour_UsageScenario())) {
            notifyEnteredSystem();
        }

        enterScenarioBehaviour(behaviour, onCompletionCallback);

        // begin simulation
        Start startAction = executor.execute(new FindActionInUsageBehaviour<Start>(behaviour, Start.class));
        simulateAction(startAction);
    }

    private void notifyAfterAction(final AbstractUserAction action, final User user) {
        listenerRegistry.getTraversalListenerList().forEach(l -> l.after(action, user));
        if (listenerRegistry.getTraversalListenerMap().containsKey(action)) {
            listenerRegistry.getTraversalListenerMap().get(action).forEach(l -> l.after(action, user));
        }
    }

    private void notifyBeforeAction(final AbstractUserAction action, final User user) {
        listenerRegistry.getTraversalListenerList().forEach(l -> l.before(action, user));
        if (listenerRegistry.getTraversalListenerMap().containsKey(action)) {
            listenerRegistry.getTraversalListenerMap().get(action).forEach(l -> l.before(action, user));
        }
    }

}

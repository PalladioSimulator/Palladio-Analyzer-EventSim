package edu.kit.ipd.sdq.eventsim.middleware;

import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simulation.IStatusObserver;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.eventsim.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationPrepareEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStartEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.components.events.EventManager;
import edu.kit.ipd.sdq.eventsim.components.events.IEventHandler;
import edu.kit.ipd.sdq.eventsim.components.events.SimulationEvent;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.MaxMeasurementsStopCondition;

/**
 * The simulation middleware is the central point of the simulation component based simulation. This
 * component is activated in the simulator launch configuration.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
@Singleton
public class SimulationMiddleware implements ISimulationMiddleware {

    private static final Logger logger = Logger.getLogger(SimulationMiddleware.class);

    @Inject
    private ISimulationModel model;

    @Inject
    private ISimulationConfiguration config;

    @Inject
    private MeasurementStorage measurementStorage;

    @Inject
    private IRandomGenerator randomNumberGenerator;

    private EventManager eventManager;

    private int measurementCount;

    @Inject
    public SimulationMiddleware(EventManager eventManager) {
        // setup event listeners early because the middleware itself listens to simulation events
        this.eventManager = eventManager;
        registerEventHandler();
    }

    private void initialize() {
        addEcoreTypeExtractor(measurementStorage);
        connectSimulationModelToSimulationEngine(model);
        setupStopConditions();
    }

    private static void addEcoreTypeExtractor(MeasurementStorage measurementStorage) {
        measurementStorage.addTypeExtractor(EObject.class, new Function<Object, String>() {
            @Override
            public String apply(Object o) {
                return stripNamespace(((EObject) o).eClass().getInstanceClassName());
            }

            private String stripNamespace(String fqn) {
                int startOfClassName = fqn.lastIndexOf(".");
                return fqn.substring(startOfClassName + 1, fqn.length());
            }
        });
    }

    private static void connectSimulationModelToSimulationEngine(ISimulationModel model) {
        ISimEngineFactory factory = SimulationPreferencesHelper.getPreferredSimulationEngine();
        if (factory == null) {
            throw new RuntimeException("There is no simulation engine available. Install at least one engine.");
        }
        model.setSimulationEngineFactory(factory);
        factory.setModel(model);
    }

    /**
     * Setup the simulation stop conditions based on the simulation configuration.
     * 
     * @param config
     *            A simulation configuration
     */
    private void setupStopConditions() {
        SimulationConfiguration configuration = (SimulationConfiguration) config;
        long maxMeasurements = configuration.getMaxMeasurementsCount();
        long maxSimulationTime = configuration.getSimuTime();

        if (maxMeasurements <= 0 && maxSimulationTime <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deactivating maximum simulation time stop condition per user request");
            }
            this.getSimulationControl().setMaxSimTime(0);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Enabling simulation stop condition at maximum simulation time of " + maxSimulationTime);
            }

            if (maxSimulationTime > 0) {
                this.getSimulationControl().setMaxSimTime(maxSimulationTime);
            }
        }

        this.getSimulationControl().addStopCondition(new MaxMeasurementsStopCondition(this));
    }

    private void registerEventHandler() {
        // on simulation start
        registerEventHandler(SimulationStartEvent.class, e -> notifyStartListeners());

        // on simulation stop
        registerEventHandler(SimulationStopEvent.class, e -> notifyStopListeners());
        registerEventHandler(SimulationStopEvent.class, e -> finalise());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startSimulation(final IStatusObserver statusObserver) {
        if (logger.isInfoEnabled()) {
            logger.info("Simulation Started");
        }
        // initialize before preparation phase so that the simulation model is already bound to the
        // simulation engine
        initialize();
        
        eventManager.triggerEvent(new SimulationPrepareEvent());
        setupSimulationProgressObserver(statusObserver);
        model.getSimulationControl().start();
    }

    private void setupSimulationProgressObserver(IStatusObserver statusObserver) {
        final long simStopTime = config.getSimuTime();
        model.getSimulationControl().addTimeObserver(new Observer() {

            public void update(final Observable clock, final Object data) {

                int timePercent = (int) (getSimulationControl().getCurrentSimulationTime() * 100 / simStopTime);
                int measurementsPercent = (int) (getMeasurementCount() * 100 / config.getMaxMeasurementsCount());

                if (timePercent < measurementsPercent) {
                    statusObserver.updateStatus(measurementsPercent,
                            (int) getSimulationControl().getCurrentSimulationTime(), getMeasurementCount());
                } else {
                    statusObserver.updateStatus(timePercent, (int) getSimulationControl().getCurrentSimulationTime(),
                            getMeasurementCount());
                }

            }

        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopSimulation() {
        model.getSimulationControl().stop();
    }

    /**
     * Called after a simulation run to perform some clean up.
     */
    private void finalise() {
        measurementStorage.finish();
        EventSimEntity.resetIdGenerator();
        notifyStopListeners();
        eventManager.unregisterAllEventHandlers();
        logger.info(
                "Simulation took " + this.getSimulationControl().getCurrentSimulationTime() + " simulation seconds");
    }

    @Override
    public void triggerEvent(SimulationEvent event) {
        // delegate event processing
        eventManager.triggerEvent(event);
    }

    @Override
    public <T extends SimulationEvent> void registerEventHandler(Class<T> eventType, final IEventHandler<T> handler,
            String filter) {
        // delegate handler registration
        eventManager.registerEventHandler(eventType, handler, filter);
    }

    @Override
    public <T extends SimulationEvent> void registerEventHandler(Class<T> eventType, final IEventHandler<T> handler) {
        registerEventHandler(eventType, handler, null);
    }

    /**
     * Gives access to the simulation configuration of the current simulation
     * 
     * @return A simulation configuration
     */
    public ISimulationConfiguration getSimulationConfiguration() {
        return this.config;
    }

    @Override
    public void increaseMeasurementCount() {
        measurementCount++;
    }

    @Override
    public int getMeasurementCount() {
        return measurementCount;
    }

    @Override
    public ISimulationModel getSimulationModel() {
        return model;
    }

    @Override
    public ISimulationControl getSimulationControl() {
        return model.getSimulationControl();
    }

    /**
     * @return
     */
    @Override
    public IRandomGenerator getRandomGenerator() {
        return randomNumberGenerator;
    }

    /**
     * Notifies all simulation observers that the simulation is about to start
     */
    private void notifyStartListeners() {
        config.getListeners().forEach(l -> l.simulationStart());
    }

    /**
     * Notifies all simulation observers that the simulation has stopped.
     */
    private void notifyStopListeners() {
        config.getListeners().forEach(l -> l.simulationStop());
    }

}
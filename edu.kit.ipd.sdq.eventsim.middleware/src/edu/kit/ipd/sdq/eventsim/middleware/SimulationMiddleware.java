package edu.kit.ipd.sdq.eventsim.middleware;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simucomframework.SimuComDefaultRandomNumberGenerator;
import de.uka.ipd.sdq.simulation.AbstractSimulationConfig;
import de.uka.ipd.sdq.simulation.ISimulationListener;
import de.uka.ipd.sdq.simulation.IStatusObserver;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.middleware.components.AbstractComponentFacade;
import edu.kit.ipd.sdq.eventsim.middleware.events.EventManager;
import edu.kit.ipd.sdq.eventsim.middleware.events.IEventHandler;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;
import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationStopEvent;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.MaxMeasurementsStopCondition;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.PCMModel;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.SimulationModel;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;

/**
 * The simulation middleware is the central point of the simulation component based simulation. This component is
 * activated in the simulator launch configuration.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
public class SimulationMiddleware extends AbstractComponentFacade implements ISimulationMiddleware {

	private static final Logger logger = Logger.getLogger(SimulationMiddleware.class);

	private SimulationModel simModel;
	private ISimulationControl simControl;
	private ISimulationConfiguration simConfig;
	private PCMModel pcmModel;
	private int measurementCount;
	private IRandomGenerator randomNumberGenerator;
	private MeasurementStorage measurementStorage;
	private EventManager eventManager;

	public SimulationMiddleware(ISimulationConfiguration config, PCMModel pcmModel) {
		provide(ISimulationMiddleware.class, this);
		
		eventManager = new EventManager();
		registerEventHandler();
		initialize(config, pcmModel);
	}

	private void initialize(ISimulationConfiguration config, PCMModel pcmModel) {
		this.pcmModel = pcmModel;
		this.simConfig = config;

		// initialize R measurement store
		measurementStorage = RMeasurementStore.fromLaunchConfiguration(config.getConfigurationMap());
		if (measurementStorage == null) {
			throw new RuntimeException("R measurement store could not bet constructed from launch configuration.");
		}

		// Create the simulation model (this model is control and not the subject of the simulation)
		ISimEngineFactory factory = SimulationPreferencesHelper.getPreferredSimulationEngine();
		if (factory == null) {
			throw new RuntimeException("There is no simulation engine available. Install at least one engine.");
		}

		this.simModel = new SimulationModel(factory, this);
		factory.setModel(simModel);
		this.simControl = simModel.getSimulationControl();

		this.setupStopConditions(config);
	}

	/**
	 * Setup the simulation stop conditions based on the simulation configuration.
	 * 
	 * @param config
	 *            A simulation configuration
	 */
	private void setupStopConditions(ISimulationConfiguration simConfig) {

		this.measurementCount = 1;
		SimulationConfiguration config = (SimulationConfiguration) simConfig;
		long maxMeasurements = config.getMaxMeasurementsCount();
		long maxSimulationTime = config.getSimuTime();

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

		// set up an observer for the simulation progress
		final SimulationConfiguration config = (SimulationConfiguration) this.getSimulationConfiguration();
		final long simStopTime = config.getSimuTime();
		this.simControl.addTimeObserver(new Observer() {

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

		notifyStartListeners(); // TODO obsolete!?
		simControl.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopSimulation() {
		simControl.stop();
	}

	/**
	 * Called after a simulation run to perform some clean up.
	 */
	private void finalise() {
		notifyStopListeners();
		measurementStorage.finish();
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
		return this.simConfig;
	}

	/**
	 * Returns the PCM model to be simulated. If it has not been loaded before, this methods loads the PCM model from
	 * the bundle.
	 * 
	 * @return a PCM model instance
	 */
	@Override
	public PCMModel getPCMModel() {
		return this.pcmModel;
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
	public SimulationModel getSimulationModel() {
		return simModel;
	}

	@Override
	public ISimulationControl getSimulationControl() {
		return simControl;
	}

	/**
	 * @return
	 */
	@Override
	public IRandomGenerator getRandomGenerator() {
		if (randomNumberGenerator == null) {
			// TODO get rid of SimuCom dependency
			randomNumberGenerator = new SimuComDefaultRandomNumberGenerator(simConfig.getRandomSeed());
		}
		return randomNumberGenerator;
	}

	@Override
	public MeasurementStorage getMeasurementStorage() {
		return measurementStorage;
	}

	/**
	 * Notfies all simulation observers that the simulation is about to start
	 */
	private void notifyStartListeners() {
		AbstractSimulationConfig config = (AbstractSimulationConfig) this.getSimulationConfiguration();
		for (final ISimulationListener l : config.getListeners()) {
			l.simulationStart();
		}
	}

	/**
	 * Notifies all simulation observers that the simulation has stopped.
	 * 
	 * TODO this method is redundant to {@link SimulationStopEvent} and should be removed
	 */
	private void notifyStopListeners() {
		AbstractSimulationConfig config = (AbstractSimulationConfig) this.getSimulationConfiguration();
		for (final ISimulationListener l : config.getListeners()) {
			l.simulationStop();
		}
	}

}
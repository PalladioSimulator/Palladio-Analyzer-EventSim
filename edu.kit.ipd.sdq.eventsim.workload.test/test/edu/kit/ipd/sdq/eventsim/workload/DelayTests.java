package edu.kit.ipd.sdq.eventsim.workload;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.Delay;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.Metric;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.workload.calculators.TimeSpanBetweenUserActionsCalculator;

public class DelayTests {

	private static final Level LOG_LEVEL = Level.DEBUG;

	private static final double DELTA = 1e-10;

	@SuppressWarnings("rawtypes")
	@Captor
	private ArgumentCaptor<Measurement> measurementArgument;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Logger.getLogger("edu.kit.ipd.sdq.eventsim").setLevel(LOG_LEVEL);
	}

	@Test
	public void delaysDontCauseContention_oneUsageScenario_twoConcurrentUsers() {
		// create PCM usage model
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();
		int closedWorkloadPopulation = 2;
		ScenarioBehaviour b = createUsageScenarioWithClosedWorkloadInUsageModel(um, closedWorkloadPopulation);
		Delay delay = createStartDelayStopActionChainInBehaviour(b);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(2).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		MeasurementStorage measurementStorage = mock(MeasurementStorage.class);
		measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator(Metric.TIME_SPAN))
				.from(delay, "before").to(delay, "after").forEachMeasurement(m -> measurementStorage.putPair(m));

		// run simulation
		manager.startSimulation();

		// check simulation results
		verify(measurementStorage, times(2)).putPair(measurementArgument.capture());
		Measurement<?, ?> firstMeasurement = measurementArgument.getAllValues().get(0);
		assertEquals(1.42, firstMeasurement.getValue(), DELTA);
		assertEquals(1.42, firstMeasurement.getWhen(), DELTA);

		Measurement<?, ?> secondMeasurement = measurementArgument.getAllValues().get(1);
		assertEquals(1.42, secondMeasurement.getValue(), DELTA);
		assertEquals(1.42, secondMeasurement.getWhen(), DELTA);

		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 1.42, DELTA);
	}

	@Test
	public void delaysDontCauseContention_twoUsageScenarios_oneUserPerScenario() {
		// create PCM usage model
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();
		int closedWorkloadPopulation = 1;
		ScenarioBehaviour b1 = createUsageScenarioWithClosedWorkloadInUsageModel(um, closedWorkloadPopulation);
		ScenarioBehaviour b2 = createUsageScenarioWithClosedWorkloadInUsageModel(um, closedWorkloadPopulation);
		Delay delay1 = createStartDelayStopActionChainInBehaviour(b1);
		Delay delay2 = createStartDelayStopActionChainInBehaviour(b2);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(2).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		MeasurementStorage measurementStorage = mock(MeasurementStorage.class);
		measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator(Metric.TIME_SPAN))
				.from(delay1, "before").to(delay1, "after").forEachMeasurement(m -> measurementStorage.putPair(m));
		measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator(Metric.TIME_SPAN))
				.from(delay2, "before").to(delay2, "after").forEachMeasurement(m -> measurementStorage.putPair(m));

		// run simulation
		manager.startSimulation();

		// check simulation results
		verify(measurementStorage, times(2)).putPair(measurementArgument.capture());
		Measurement<?, ?> firstMeasurement = measurementArgument.getAllValues().get(0);
		assertEquals(1.42, firstMeasurement.getValue(), DELTA);
		assertEquals(1.42, firstMeasurement.getWhen(), DELTA);

		Measurement<?, ?> secondMeasurement = measurementArgument.getAllValues().get(1);
		assertEquals(1.42, secondMeasurement.getValue(), DELTA);
		assertEquals(1.42, secondMeasurement.getWhen(), DELTA);

		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 1.42, DELTA);
	}

	private Delay createStartDelayStopActionChainInBehaviour(ScenarioBehaviour b) {
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		start.setScenarioBehaviour_AbstractUserAction(b);

		Delay delay = UsagemodelFactory.eINSTANCE.createDelay();
		delay.setScenarioBehaviour_AbstractUserAction(b);
		delay.setPredecessor(start);

		PCMRandomVariable delayTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		delayTime.setSpecification("1.42");
		delayTime.setDelay_TimeSpecification(delay);

		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		stop.setScenarioBehaviour_AbstractUserAction(b);
		stop.setPredecessor(delay);
		return delay;
	}

	private ScenarioBehaviour createUsageScenarioWithClosedWorkloadInUsageModel(UsageModel um, int population) {
		UsageScenario s = UsagemodelFactory.eINSTANCE.createUsageScenario();
		s.setUsageModel_UsageScenario(um);

		ClosedWorkload w = UsagemodelFactory.eINSTANCE.createClosedWorkload();
		w.setUsageScenario_Workload(s);
		w.setPopulation(population);

		PCMRandomVariable thinkTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		thinkTime.setSpecification("0");
		thinkTime.setClosedWorkload_PCMRandomVariable(w);

		ScenarioBehaviour b = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
		b.setUsageScenario_SenarioBehaviour(s);
		return b;
	}

}

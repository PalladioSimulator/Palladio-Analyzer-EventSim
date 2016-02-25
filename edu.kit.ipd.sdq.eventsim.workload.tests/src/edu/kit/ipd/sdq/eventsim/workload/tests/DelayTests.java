package edu.kit.ipd.sdq.eventsim.workload.tests;

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
import org.palladiosimulator.pcm.usagemodel.Delay;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.kit.ipd.sdq.eventsim.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;
import edu.kit.ipd.sdq.eventsim.test.util.builder.ConfigurationBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.PCMModelBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.UsageBuilder;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModel;
import edu.kit.ipd.sdq.eventsim.workload.calculators.TimeSpanBetweenUserActionsCalculator;

/**
 * Tests simulation of {@link Delay} actions.
 * 
 * @author Philipp Merkle
 *
 */
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
		BuildingContext ctx = new BuildingContext();
		UsageBuilder ub = ctx.newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(2, 0).buildIn(um);
		ub.newBehaviour().start().delay("delay", 1.42).stop().buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(2).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Delay delay = ctx.lookup(UsagemodelPackage.eINSTANCE.getDelay(), "delay");
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		MeasurementStorage measurementStorage = mock(MeasurementStorage.class);
		measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator("TIME_SPAN"))
				.from(delay, "before").to(delay, "after").forEachMeasurement(m -> measurementStorage.put(m));

		// run simulation
		manager.startSimulation();

		// check simulation results
		verify(measurementStorage, times(2)).put(measurementArgument.capture());
		Measurement<?> firstMeasurement = measurementArgument.getAllValues().get(0);
		assertEquals(1.42, firstMeasurement.getValue(), DELTA);
		assertEquals(1.42, firstMeasurement.getWhen(), DELTA);

		Measurement<?> secondMeasurement = measurementArgument.getAllValues().get(1);
		assertEquals(1.42, secondMeasurement.getValue(), DELTA);
		assertEquals(1.42, secondMeasurement.getWhen(), DELTA);

		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 1.42, DELTA);
	}

	@Test
	public void delaysDontCauseContention_twoUsageScenarios_oneUserPerScenario() {
		// create PCM usage model
		BuildingContext ctx = new BuildingContext();
		UsageBuilder ub = ctx.newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s1 = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start().delay("delay1", 1.42).stop().buildIn(s1);
		UsageScenario s2 = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start().delay("delay2", 1.42).stop().buildIn(s2);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(2).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Delay delay1 = ctx.lookup(UsagemodelPackage.eINSTANCE.getDelay(), "delay1");
		Delay delay2 = ctx.lookup(UsagemodelPackage.eINSTANCE.getDelay(), "delay2");
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		MeasurementStorage measurementStorage = mock(MeasurementStorage.class);
		measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator("TIME_SPAN"))
				.from(delay1, "before").to(delay1, "after").forEachMeasurement(m -> measurementStorage.put(m));
		measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator("TIME_SPAN"))
				.from(delay2, "before").to(delay2, "after").forEachMeasurement(m -> measurementStorage.put(m));

		// run simulation
		manager.startSimulation();

		// check simulation results
		verify(measurementStorage, times(2)).put(measurementArgument.capture());
		Measurement<?> firstMeasurement = measurementArgument.getAllValues().get(0);
		assertEquals(1.42, firstMeasurement.getValue(), DELTA);
		assertEquals(1.42, firstMeasurement.getWhen(), DELTA);

		Measurement<?> secondMeasurement = measurementArgument.getAllValues().get(1);
		assertEquals(1.42, secondMeasurement.getValue(), DELTA);
		assertEquals(1.42, secondMeasurement.getWhen(), DELTA);

		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 1.42, DELTA);
	}

}

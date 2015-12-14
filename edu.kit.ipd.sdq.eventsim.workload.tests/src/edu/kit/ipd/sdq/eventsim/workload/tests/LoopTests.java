package edu.kit.ipd.sdq.eventsim.workload.tests;

import static edu.kit.ipd.sdq.eventsim.test.util.matcher.BeforeMatcher.before;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.test.util.Tracer;
import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;
import edu.kit.ipd.sdq.eventsim.test.util.builder.ConfigurationBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.PCMModelBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.UsageBuilder;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModel;

/**
 * Tests simulation of {@link Loop} actions.
 * 
 * @author Philipp Merkle
 *
 */
public class LoopTests {

	private static final Level LOG_LEVEL = Level.INFO;

	private static final double DELTA = 1e-10;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() {
		Logger.getLogger("edu.kit.ipd.sdq.eventsim").setLevel(LOG_LEVEL);
	}

	@Test
	public void simpleLoop() {
		final int LOOP_ITERATIONS = 23;

		// create PCM usage model
		UsageBuilder ub = new BuildingContext().usageBuilder();
		UsageModel um = ub.build();
		UsageScenario s = ub.scenarioBuilder().closedWorkload(1, 0).buildIn(um);
		ub.behaviourBuilder().start("outer_start").loop("loop", LOOP_ITERATIONS).stop("outer_stop").buildIn(s);
		ub.behaviourBuilder().start("inner_start").stop("inner_stop").buildAsLoopBehaviourIn("loop");
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		Tracer trace = Tracer.forUserActions(um, measurementFacade);

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		assertThat(trace.size(), equalTo(3 + LOOP_ITERATIONS * 2));
		assertThat(trace.firstInvocationOf("outer_start"), before(trace.firstInvocationOf("loop")));
		assertThat(trace.firstInvocationOf("loop"), before(trace.firstInvocationOf("inner_start")));
		assertThat(trace.firstInvocationOf("inner_start"), before(trace.firstInvocationOf("inner_stop")));
		assertThat(trace.firstInvocationOf("inner_stop"), before(trace.firstInvocationOf("outer_stop")));

		assertThat(trace.invocationCount("inner_start"), equalTo(23));
		assertThat(trace.invocationCount("inner_stop"), equalTo(23));

		// simulated time should not have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 0, DELTA);
	}

	@Test
	public void delayInLoopShouldAdvanceSimulationTime() {
		final double DELAY_TIME = 1.0;
		final int LOOP_ITERATIONS = 23;

		// create PCM usage model
		UsageBuilder ub = new BuildingContext().usageBuilder();
		UsageModel um = ub.build();
		UsageScenario s = ub.scenarioBuilder().closedWorkload(1, 0).buildIn(um);
		ub.behaviourBuilder().start().loop("loop", LOOP_ITERATIONS).stop("stop").buildIn(s);
		ub.behaviourBuilder().start().delay(DELAY_TIME).stop().buildAsLoopBehaviourIn("loop");
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		Tracer trace = Tracer.forUserActions(um, measurementFacade);

		// TODO perhaps additionally check response time measurement results

		// run simulation
		manager.startSimulation();

		// simulated time should have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(),
				LOOP_ITERATIONS * DELAY_TIME, DELTA);

		// make sure that stop action has been visited
		assertThat(trace.invocationCount("stop"), equalTo(1));
	}

	@Test
	public void loopWithoutBehaviourShouldBeSkippedWithoutException() {
		final int LOOP_ITERATIONS = 23;

		// create PCM usage model
		UsageBuilder ub = new BuildingContext().usageBuilder();
		UsageModel um = ub.build();
		UsageScenario s = ub.scenarioBuilder().closedWorkload(1, 0).buildIn(um);
		ub.behaviourBuilder().start("start").loop("loop", LOOP_ITERATIONS).stop("stop").buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		Tracer trace = Tracer.forUserActions(um, measurementFacade);

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		assertThat(trace.size(), equalTo(3));
		assertThat(trace.firstInvocationOf("start"), before(trace.firstInvocationOf("loop")));
		assertThat(trace.firstInvocationOf("loop"), before(trace.firstInvocationOf("stop")));
	}

}

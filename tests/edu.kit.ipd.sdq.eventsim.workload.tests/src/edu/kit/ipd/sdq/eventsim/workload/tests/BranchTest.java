package edu.kit.ipd.sdq.eventsim.workload.tests;

import static edu.kit.ipd.sdq.eventsim.test.util.matcher.ApproximatelyMatcher.approximately;
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
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.kit.ipd.sdq.eventsim.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.InvalidModelParametersException;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.test.util.Tracer;
import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;
import edu.kit.ipd.sdq.eventsim.test.util.builder.ConfigurationBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.PCMModelBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.UsageBuilder;

/**
 * Tests simulation of {@link Branch} actions.
 * 
 * @author Philipp Merkle
 *
 */
public class BranchTest {

	private static final Level LOG_LEVEL = Level.INFO;

	private static final double DELTA = 1e-10;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() {
		Logger.getLogger("edu.kit.ipd.sdq.eventsim").setLevel(LOG_LEVEL);
	}

	@Test
	public void oneBranchTransitionTest() {
		// create PCM usage model
		UsageBuilder ub = new BuildingContext().newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start("outer_start").branch("branch").stop("outer_stop").buildIn(s);
		ub.newBehaviour().start("inner_start").stop("inner_stop").buildAsTransitionIn("branch", 1);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new SimulationModuleTest(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Tracer trace = new Tracer(manager).instrumentUserActions(um);

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		assertThat(trace.size(), equalTo(5));
		assertThat(trace.firstInvocationOf("outer_start"), before(trace.firstInvocationOf("inner_start")));
		assertThat(trace.firstInvocationOf("inner_start"), before(trace.firstInvocationOf("inner_stop")));
		assertThat(trace.firstInvocationOf("inner_stop"), before(trace.firstInvocationOf("outer_stop")));

		// simulated time should not have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 0, DELTA);
	}

	@Test
	public void twoBranchTransitionsTest() {
		// create PCM usage model
		UsageBuilder ub = new BuildingContext().newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start().branch("branch").stop().buildIn(s);
		ub.newBehaviour().start("start_left").stop().buildAsTransitionIn("branch", 0.75);
		ub.newBehaviour().start("start_right").stop().buildAsTransitionIn("branch", 0.25);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1000).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new SimulationModuleTest(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Tracer trace = new Tracer(manager).instrumentUserActions(um);

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		assertThat(trace.invocationCount("start_left") + trace.invocationCount("start_right"), equalTo(1000));

		int tolerance = 50;
		assertThat(trace.invocationCount("start_left"), approximately(750, tolerance));
		assertThat(trace.invocationCount("start_right"), approximately(250, tolerance));

		// simulated time should not have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 0, DELTA);
	}

	@Test
	public void delayInBranchTransitionShouldAdvanceSimulationTimeTest() {
		final double DELAY_TIME = 1.23;

		// create PCM usage model
		UsageBuilder ub = new BuildingContext().newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start().branch("branch").stop("stop").buildIn(s);
		ub.newBehaviour().start().delay(DELAY_TIME).stop().buildAsTransitionIn("branch", 1);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new SimulationModuleTest(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Tracer trace = new Tracer(manager).instrumentUserActions(um);

		// TODO perhaps additionally check response time measurement results

		// run simulation
		manager.startSimulation();

		// simulated time should have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), DELAY_TIME, DELTA);

		// make sure that stop action has been visited
		assertThat(trace.invocationCount("stop"), equalTo(1));
	}

	@Test
	public void branchWithoutBranchTransitionsShouldBeSkippedWithoutExceptionTest() {
		// create PCM usage model
		UsageBuilder ub = new BuildingContext().newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start("start").branch("branch").stop("stop").buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new SimulationModuleTest(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Tracer trace = new Tracer(manager).instrumentUserActions(um);

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		assertThat(trace.size(), equalTo(3));
		assertThat(trace.firstInvocationOf("start"), before(trace.firstInvocationOf("branch")));
		assertThat(trace.firstInvocationOf("branch"), before(trace.firstInvocationOf("stop")));
	}

	@Test
	public void throwExceptionWhenSumOfBranchingProbabilitiesIsSmallerOneTest() {
		// create PCM usage model
		UsageBuilder ub = new BuildingContext().newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start().branch("branch").stop().buildIn(s);
		ub.newBehaviour().start().stop().buildAsTransitionIn("branch", 0.5);
		ub.newBehaviour().start().stop().buildAsTransitionIn("branch", 0.4);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new SimulationModuleTest(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// run simulation
		thrown.expect(InvalidModelParametersException.class);
		manager.startSimulation();
	}

	@Test
	public void throwExceptionWhenSumOfBranchingProbabilitiesIsLargerOneTest() {
		// create PCM usage model
		UsageBuilder ub = new BuildingContext().newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start().branch("branch").stop().buildIn(s);
		ub.newBehaviour().start().stop().buildAsTransitionIn("branch", 0.5);
		ub.newBehaviour().start().stop().buildAsTransitionIn("branch", 0.6);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new SimulationModuleTest(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// run simulation
		thrown.expect(InvalidModelParametersException.class);
		manager.startSimulation();
	}

}

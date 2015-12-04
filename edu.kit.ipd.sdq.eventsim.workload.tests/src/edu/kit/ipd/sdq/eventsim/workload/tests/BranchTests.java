package edu.kit.ipd.sdq.eventsim.workload.tests;

import static edu.kit.ipd.sdq.eventsim.workload.tests.utils.ApproximatelyMatcher.approximately;
import static edu.kit.ipd.sdq.eventsim.workload.tests.utils.BeforeMatcher.before;
import static edu.kit.ipd.sdq.eventsim.workload.tests.utils.ScenarioBehaviourBuilder.transition;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.InvalidModelParametersException;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModel;
import edu.kit.ipd.sdq.eventsim.workload.tests.utils.ConfigurationBuilder;
import edu.kit.ipd.sdq.eventsim.workload.tests.utils.PCMModelBuilder;
import edu.kit.ipd.sdq.eventsim.workload.tests.utils.ScenarioBehaviourBuilder;
import edu.kit.ipd.sdq.eventsim.workload.tests.utils.Tracer;
import edu.kit.ipd.sdq.eventsim.workload.tests.utils.UsageScenarioBuilder;

/**
 * Tests simulation of {@link Branch} actions.
 * 
 * @author Philipp Merkle
 *
 */
public class BranchTests {

	private static final Level LOG_LEVEL = Level.INFO;

	private static final double DELTA = 1e-10;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Logger.getLogger("edu.kit.ipd.sdq.eventsim").setLevel(LOG_LEVEL);
	}

	@Test
	public void oneBranchTransition() {
		// create PCM usage model
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();
		UsageScenario s = new UsageScenarioBuilder().closedWorkload(1, 0).buildIn(um);
		BranchTransition t = transition(1,
				new ScenarioBehaviourBuilder().start("inner_start").stop("inner_stop").build());
		new ScenarioBehaviourBuilder().start("outer_start").branch(t).stop("outer_stop").buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		Tracer trace = new Tracer(measurementFacade);
		trace.instrumentAllUserActions(um);

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
	public void twoBranchTransition() {
		// create PCM usage model
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();
		UsageScenario s = new UsageScenarioBuilder().closedWorkload(1, 0).buildIn(um);
		BranchTransition t1 = transition(0.75, new ScenarioBehaviourBuilder().start("start_left").stop().build());
		BranchTransition t2 = transition(0.25, new ScenarioBehaviourBuilder().start("start_right").stop().build());
		new ScenarioBehaviourBuilder().start().branch(t1, t2).stop().buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1000).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		Tracer trace = new Tracer(measurementFacade);
		trace.instrumentAllUserActions(um);

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		assertThat(trace.invocationCount("start_left") + trace.invocationCount("start_right"), equalTo(1000));

		int tolerance = 25;
		assertThat(trace.invocationCount("start_left"), approximately(750, tolerance));
		assertThat(trace.invocationCount("start_right"), approximately(250, tolerance));

		// simulated time should not have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 0, DELTA);
	}

	@Test
	public void branchWithoutBranchTransitionsShouldBeSkippedWithoutException() {
		// create PCM usage model
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();
		UsageScenario s = new UsageScenarioBuilder().closedWorkload(1, 0).buildIn(um);
		new ScenarioBehaviourBuilder().start("start").branch("branch").stop("stop").buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();
	
		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();
	
		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);
	
		// set up custom measuring points
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		Tracer trace = new Tracer(measurementFacade);
		trace.instrumentAllUserActions(um);
	
		// run simulation
		manager.startSimulation();
	
		// check that actions have been simulated entirely and in the expected order
		assertThat(trace.size(), equalTo(3));
		assertThat(trace.firstInvocationOf("start"), before(trace.firstInvocationOf("branch")));
		assertThat(trace.firstInvocationOf("branch"), before(trace.firstInvocationOf("stop")));
	}

	@Test
	public void throwExceptionWhenSumOfBranchingProbabilitiesIsSmallerOne() {
		// create PCM usage model
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();
		UsageScenario s = new UsageScenarioBuilder().closedWorkload(1, 0).buildIn(um);
		BranchTransition t1 = transition(0.5, new ScenarioBehaviourBuilder().start().stop().build());
		BranchTransition t2 = transition(0.4, new ScenarioBehaviourBuilder().start().stop().build());
		new ScenarioBehaviourBuilder().start().branch(t1, t2).stop().buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// run simulation
		thrown.expect(InvalidModelParametersException.class);
		manager.startSimulation();
	}

	@Test
	public void throwExceptionWhenSumOfBranchingProbabilitiesIsLargerOne() {
		// create PCM usage model
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();
		UsageScenario s = new UsageScenarioBuilder().closedWorkload(1, 0).buildIn(um);
		BranchTransition t1 = transition(0.5, new ScenarioBehaviourBuilder().start().stop().build());
		BranchTransition t2 = transition(0.6, new ScenarioBehaviourBuilder().start().stop().build());
		new ScenarioBehaviourBuilder().start().branch(t1, t2).stop().buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// run simulation
		thrown.expect(InvalidModelParametersException.class);
		manager.startSimulation();
	}

}

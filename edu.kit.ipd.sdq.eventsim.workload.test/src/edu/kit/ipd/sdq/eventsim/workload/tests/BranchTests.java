package edu.kit.ipd.sdq.eventsim.workload.tests;

import static edu.kit.ipd.sdq.eventsim.workload.tests.ScenarioBehaviourBuilder.actionByName;
import static edu.kit.ipd.sdq.eventsim.workload.tests.ScenarioBehaviourBuilder.transition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
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
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
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
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModel;

/**
 * Tests simulation of {@link Branch} actions.
 * 
 * @author Philipp Merkle
 *
 */
public class BranchTests {

	private static final Level LOG_LEVEL = Level.DEBUG;

	private static final double DELTA = 1e-10;

	@Captor
	private ArgumentCaptor<Measurement<?, ?>> measurementArgument;

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
		ScenarioBehaviour b = new ScenarioBehaviourBuilder().start("outer_start").branch(t).stop("outer_stop")
				.buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Start outerStart = (Start) actionByName(b, "outer_start");
		Stop outerStop = (Stop) actionByName(b, "outer_stop");
		Start innerStart = (Start) actionByName(t, "inner_start");
		Stop innerStop = (Stop) actionByName(t, "inner_stop");
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		MeasurementStorage measurementStorage = mock(MeasurementStorage.class);
		measurementFacade.createProbe(outerStart, "before").forEachMeasurement(m -> measurementStorage.put(m));
		measurementFacade.createProbe(innerStart, "before").forEachMeasurement(m -> measurementStorage.put(m));
		measurementFacade.createProbe(innerStop, "before").forEachMeasurement(m -> measurementStorage.put(m));
		measurementFacade.createProbe(outerStop, "before").forEachMeasurement(m -> measurementStorage.put(m));

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		verify(measurementStorage, times(4)).put(measurementArgument.capture());
		assertSame(outerStart, measurementArgument.getAllValues().get(0).getWhere().getElement());
		assertSame(innerStart, measurementArgument.getAllValues().get(1).getWhere().getElement());
		assertSame(innerStop, measurementArgument.getAllValues().get(2).getWhere().getElement());
		assertSame(outerStop, measurementArgument.getAllValues().get(3).getWhere().getElement());

		// simulated time should not have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 0, DELTA);
	}

}

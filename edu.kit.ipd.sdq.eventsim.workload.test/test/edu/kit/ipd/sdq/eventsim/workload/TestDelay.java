package edu.kit.ipd.sdq.eventsim.workload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.resourcetype.ResourcetypeFactory;
import org.palladiosimulator.pcm.system.SystemFactory;
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

public class TestDelay {

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
	public void delaysDontCauseContentionTest() {
		UsageModel um = UsagemodelFactory.eINSTANCE.createUsageModel();

		UsageScenario s = UsagemodelFactory.eINSTANCE.createUsageScenario();
		s.setUsageModel_UsageScenario(um);

		ClosedWorkload w = UsagemodelFactory.eINSTANCE.createClosedWorkload();
		w.setUsageScenario_Workload(s);
		w.setPopulation(2);

		PCMRandomVariable thinkTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		thinkTime.setSpecification("0");
		thinkTime.setClosedWorkload_PCMRandomVariable(w);

		ScenarioBehaviour b = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
		b.setUsageScenario_SenarioBehaviour(s);

		Start start = UsagemodelFactory.eINSTANCE.createStart();
		start.setScenarioBehaviour_AbstractUserAction(b);

		Delay delay = UsagemodelFactory.eINSTANCE.createDelay();
		delay.setScenarioBehaviour_AbstractUserAction(b);
		delay.setPredecessor(start);

		PCMRandomVariable thinkTimeDelay = CoreFactory.eINSTANCE.createPCMRandomVariable();
		thinkTimeDelay.setSpecification("1");
		thinkTimeDelay.setDelay_TimeSpecification(delay);

		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		stop.setScenarioBehaviour_AbstractUserAction(b);
		stop.setPredecessor(delay);

		PCMModel model = new PCMModel(AllocationFactory.eINSTANCE.createAllocation(),
				RepositoryFactory.eINSTANCE.createRepository(),
				ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment(),
				SystemFactory.eINSTANCE.createSystem(), um, ResourcetypeFactory.eINSTANCE.createResourceRepository());

		SimulationConfiguration config = new SimulationConfigurationBuilder(model).stopAtMeasurementCount(2)
				.buildConfiguration();
		
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();

		MeasurementStorage measurementStorage = mock(MeasurementStorage.class);
		measurementFacade.createCalculator(new TimeSpanBetweenUserActionsCalculator(Metric.TIME_SPAN))
				.from(delay, "before").to(delay, "after").forEachMeasurement(m -> measurementStorage.putPair(m));

		manager.startSimulation();

		verify(measurementStorage, times(2)).putPair(measurementArgument.capture());
		Measurement<?, ?> firstMeasurement = measurementArgument.getAllValues().get(0);
		assertEquals(1.0, firstMeasurement.getValue(), DELTA);
		assertEquals(1.0, firstMeasurement.getWhen(), DELTA);

		Measurement<?, ?> secondMeasurement = measurementArgument.getAllValues().get(1);
		assertEquals(1.0, secondMeasurement.getValue(), DELTA);
		assertEquals(1.0, secondMeasurement.getWhen(), DELTA);
	}

}

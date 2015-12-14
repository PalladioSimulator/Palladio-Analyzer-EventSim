package edu.kit.ipd.sdq.eventsim.workload.tests;

import static edu.kit.ipd.sdq.eventsim.test.util.matcher.BeforeMatcher.before;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static edu.kit.ipd.sdq.eventsim.test.util.builder.repository.RepositoryBuilder.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.test.util.Tracer;
import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;
import edu.kit.ipd.sdq.eventsim.test.util.builder.ConfigurationBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.PCMModelBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.repository.BasicComponentBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.repository.OperationSignatureBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.repository.RepositoryBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.repository.ResourceDemandingSEFFBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.system.SystemBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.ScenarioBehaviourBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.UsageBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.UsageScenarioBuilder;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModel;

/**
 * Tests simulation of {@link EntryLevelSystemCall} actions.
 * 
 * @author Philipp Merkle
 *
 */
public class EntryLevelSystemCallTests {

	private static final Level LOG_LEVEL = Level.INFO;

	private static final double DELTA = 1e-10;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() {
		Logger.getLogger("edu.kit.ipd.sdq.eventsim").setLevel(LOG_LEVEL);
	}
	
	@Test
	public void noParameters() {
		BuildingContext ctx = new BuildingContext();
		
		// repository model
		RepositoryBuilder rb = ctx.repositoryBuilder();
		Repository r = rb.build();
		OperationInterface iface = rb.interfaze().buildIn(r);
		OperationSignature signature = rb.signature().buildIn(iface);
		ResourceDemandingSEFF seff = rb.seff(signature).start().stop().buildSeff(); 
		BasicComponent comp = rb.basicComponent().provide("inner_role", iface).seff(seff).buildIn(r);

		// system model
		SystemBuilder sb = ctx.systemBuilder();
		System sys = sb.build();
		sb.deploy("context", comp).provide("outer_role", "inner_role", "context");
		
		// usage model
		UsageBuilder ub = ctx.usageBuilder();
		UsageModel um = ub.build();
		UsageScenario s = ub.scenarioBuilder().closedWorkload(1, 0).buildIn(um);
		ub.behaviourBuilder().start().call(signature, "outer_role").stop().buildIn(s);
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
//		assertThat(trace.size(), equalTo(3 + LOOP_ITERATIONS * 2));
//		assertThat(trace.firstInvocationOf("outer_start"), before(trace.firstInvocationOf("loop")));
//		assertThat(trace.firstInvocationOf("loop"), before(trace.firstInvocationOf("inner_start")));
//		assertThat(trace.firstInvocationOf("inner_start"), before(trace.firstInvocationOf("inner_stop")));
//		assertThat(trace.firstInvocationOf("inner_stop"), before(trace.firstInvocationOf("outer_stop")));
//
//		assertThat(trace.invocationCount("inner_start"), equalTo(23));
//		assertThat(trace.invocationCount("inner_stop"), equalTo(23));

		// simulated time should not have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 0, DELTA);
	}
	
}

package edu.kit.ipd.sdq.eventsim.workload.tests;

import static edu.kit.ipd.sdq.eventsim.test.util.matcher.BeforeMatcher.before;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.composition.CompositionPackage;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.config.SimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.test.util.Tracer;
import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;
import edu.kit.ipd.sdq.eventsim.test.util.builder.ConfigurationBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.PCMModelBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.repository.RepositoryBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.system.SystemBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.UsageBuilder;

/**
 * Tests simulation of {@link EntryLevelSystemCall} actions.
 * 
 * @author Philipp Merkle
 *
 */
public class EntryLevelSystemCallTests {

	private static final Level LOG_LEVEL = Level.DEBUG;

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
		RepositoryBuilder rb = ctx.newRepositoryModel();
		Repository r = rb.build();
		OperationInterface iface = rb.newInterface().buildIn(r);
		OperationSignature signature = rb.newSignature().buildIn(iface);
		ResourceDemandingSEFF seff = rb.newSEFF(signature).start("seff_start").stop("seff_stop").buildSeff();
		BasicComponent comp = rb.newBasicComponent().provide("inner_role", iface).seff(seff).buildIn(r);

		// system model
		SystemBuilder sb = ctx.newSystemModel();
		System sys = sb.build();
		sb.deploy("context", comp).provide("outer_role", "inner_role", "context");

		// resource environment model
		ResourceEnvironment re = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
		ResourceContainer rc = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
		rc.setResourceEnvironment_ResourceContainer(re);

		// allocation model
		Allocation a = AllocationFactory.eINSTANCE.createAllocation();
		a.setSystem_Allocation(sys);
		a.setTargetResourceEnvironment_Allocation(re);

		AllocationContext allocCtx = AllocationFactory.eINSTANCE.createAllocationContext();
		allocCtx.setAllocation_AllocationContext(a);
		allocCtx.setAssemblyContext_AllocationContext(
				ctx.lookup(CompositionPackage.eINSTANCE.getAssemblyContext(), "context"));
		allocCtx.setResourceContainer_AllocationContext(rc);

		// usage model
		UsageBuilder ub = ctx.newUsageModel();
		UsageModel um = ub.build();
		UsageScenario s = ub.newScenario().closedWorkload(1, 0).buildIn(um);
		ub.newBehaviour().start("usage_start").call(signature, "outer_role").stop("usage_stop").buildIn(s);
		PCMModel model = new PCMModelBuilder().withUsageModel(um).withAllocationModel(a).build();

		// create simulation configuration
		SimulationConfiguration config = new ConfigurationBuilder(model).stopAtMeasurementCount(1).build();

		// assemble simulation components (some of them being mocked)
		Injector injector = Guice.createInjector(new TestSimulationModule(config));
		SimulationManager manager = injector.getInstance(SimulationManager.class);

		// set up custom measuring points
		Tracer trace = new Tracer(manager).instrumentUserActions(um).instrumentSeffActions(r);

		// run simulation
		manager.startSimulation();

		// check that actions have been simulated entirely and in the expected order
		// assertThat(trace.size(), equalTo(3 + LOOP_ITERATIONS * 2));
		assertThat(trace.firstInvocationOf("usage_start"), before(trace.firstInvocationOf("seff_start")));
		assertThat(trace.firstInvocationOf("seff_start"), before(trace.firstInvocationOf("seff_stop")));
		assertThat(trace.firstInvocationOf("seff_stop"), before(trace.firstInvocationOf("usage_stop")));

		// assertThat(trace.invocationCount("inner_start"), equalTo(23));
		// assertThat(trace.invocationCount("inner_stop"), equalTo(23));

		// simulated time should not have advanced
		assertEquals(manager.getMiddleware().getSimulationControl().getCurrentSimulationTime(), 0, DELTA);
	}

}

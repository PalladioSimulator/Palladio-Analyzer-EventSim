package edu.kit.ipd.sdq.eventsim.test.util.builder.system;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.core.composition.CompositionPackage;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemFactory;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;

public class SystemBuilder {

	private BuildingContext context;
	
	private System system;

	public SystemBuilder(BuildingContext context) {
		this.context = context;
		system = SystemFactory.eINSTANCE.createSystem();
		context.add(system);
	}

	public SystemBuilder deploy(String assemblyContextName, BasicComponent c) {
		AssemblyContext ctx = CompositionFactory.eINSTANCE.createAssemblyContext();
		ctx.setEntityName(assemblyContextName);
		ctx.setEncapsulatedComponent__AssemblyContext(c);
		ctx.setParentStructure__AssemblyContext(system);
		return this;
	}

	public SystemBuilder provide(String outerRoleName, String innerRoleName, String providingCtxName) {
		OperationProvidedRole innerRole = context.lookup(RepositoryPackage.eINSTANCE.getOperationProvidedRole(), innerRoleName);
		AssemblyContext providingCtx = context.lookup(CompositionPackage.eINSTANCE.getAssemblyContext(), providingCtxName);
		
		OperationProvidedRole outerRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		outerRole.setEntityName(outerRoleName);
		outerRole.setProvidedInterface__OperationProvidedRole(innerRole.getProvidedInterface__OperationProvidedRole());
		outerRole.setProvidingEntity_ProvidedRole(system);

		ProvidedDelegationConnector c = CompositionFactory.eINSTANCE.createProvidedDelegationConnector();
		c.setAssemblyContext_ProvidedDelegationConnector(providingCtx);
		c.setInnerProvidedRole_ProvidedDelegationConnector(innerRole);
		c.setOuterProvidedRole_ProvidedDelegationConnector(outerRole);
		c.setParentStructure__Connector(system);

		return this;
	}

	public System build() {
		return system;
	}

}

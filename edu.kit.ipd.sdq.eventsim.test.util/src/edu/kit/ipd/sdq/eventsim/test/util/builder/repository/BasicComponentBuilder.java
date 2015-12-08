package edu.kit.ipd.sdq.eventsim.test.util.builder.repository;

import java.util.UUID;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

public class BasicComponentBuilder {

	private BasicComponent component;

	public BasicComponentBuilder() {
		this(randomName());
	}

	public BasicComponentBuilder(String name) {
		component = RepositoryFactory.eINSTANCE.createBasicComponent();
		component.setEntityName(name);
	}

	public BasicComponentBuilder provide(OperationInterface iface) {
		OperationProvidedRole role = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		role.setEntityName(randomName());
		role.setProvidedInterface__OperationProvidedRole(iface);
		role.setProvidingEntity_ProvidedRole(component);
		return this;
	}

	public BasicComponentBuilder require(OperationInterface iface) {
		OperationRequiredRole role = RepositoryFactory.eINSTANCE.createOperationRequiredRole();
		role.setEntityName(randomName());
		role.setRequiredInterface__OperationRequiredRole(iface);
		role.setRequiringEntity_RequiredRole(component);
		return this;
	}
	
	public BasicComponentBuilder seff(ResourceDemandingSEFF seff) {
		seff.setBasicComponent_ServiceEffectSpecification(component);
		return this;		
	}

	public BasicComponent buildIn(Repository repository) {
		component.setRepository__RepositoryComponent(repository);
		return component;
	}

	private static String randomName() {
		return UUID.randomUUID().toString();
	}

}

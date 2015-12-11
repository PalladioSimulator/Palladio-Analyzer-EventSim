package edu.kit.ipd.sdq.eventsim.test.util.builder.repository;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;
import edu.kit.ipd.sdq.eventsim.test.util.builder.ModelBuilderUtil;

public class BasicComponentBuilder {

	private BuildingContext context;
	
	private BasicComponent component;

	public BasicComponentBuilder(BuildingContext context) {
		this(ModelBuilderUtil.randomName(), context);
	}

	public BasicComponentBuilder(String name, BuildingContext context) {
		this.context = context;
		component = RepositoryFactory.eINSTANCE.createBasicComponent();
		component.setEntityName(name);
		context.add(component);
	}

	public BasicComponentBuilder provide(String name, OperationInterface iface) {
		OperationProvidedRole role = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		role.setEntityName(name);
		role.setProvidedInterface__OperationProvidedRole(iface);
		role.setProvidingEntity_ProvidedRole(component);
		return this;
	}

	public BasicComponentBuilder provide(OperationInterface iface) {
		return provide(ModelBuilderUtil.randomName(), iface);
	}

	public BasicComponentBuilder require(String name, OperationInterface iface) {
		OperationRequiredRole role = RepositoryFactory.eINSTANCE.createOperationRequiredRole();
		role.setEntityName(name);
		role.setRequiredInterface__OperationRequiredRole(iface);
		role.setRequiringEntity_RequiredRole(component);
		return this;
	}

	public BasicComponentBuilder require(OperationInterface iface) {
		return require(ModelBuilderUtil.randomName(), iface);
	}

	public BasicComponentBuilder seff(ResourceDemandingSEFF seff) {
		seff.setBasicComponent_ServiceEffectSpecification(component);
		return this;
	}

	public BasicComponent buildIn(Repository repository) {
		component.setRepository__RepositoryComponent(repository);
		return component;
	}

}

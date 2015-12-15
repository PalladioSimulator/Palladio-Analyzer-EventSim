package edu.kit.ipd.sdq.eventsim.test.util.builder.repository;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;
import edu.kit.ipd.sdq.eventsim.test.util.builder.ModelBuilderUtil;

public class RepositoryBuilder {

	private BuildingContext context;

	private Repository repository;

	public RepositoryBuilder(BuildingContext context) {
		this.context = context;
		repository = RepositoryFactory.eINSTANCE.createRepository();
		context.add(repository);
	}

	public BasicComponentBuilder newBasicComponent(String name) {
		return new BasicComponentBuilder(name, context);
	}

	public BasicComponentBuilder newBasicComponent() {
		return newBasicComponent(ModelBuilderUtil.randomName());
	}

	public ResourceDemandingSEFFBuilder newSEFF(OperationSignature signature) {
		return new ResourceDemandingSEFFBuilder(signature, context);
	}

	public OperationSignatureBuilder newSignature(String name) {
		return new OperationSignatureBuilder(name, context);
	}

	public OperationSignatureBuilder newSignature() {
		return newSignature(ModelBuilderUtil.randomName());
	}

	public OperationInterfaceBuilder newInterface(String name) {
		return new OperationInterfaceBuilder(name, context);
	}

	public OperationInterfaceBuilder newInterface() {
		return newInterface(ModelBuilderUtil.randomName());
	}

	public Repository build() {
		return repository;
	}

}

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

	public BasicComponentBuilder basicComponent(String name) {
		return new BasicComponentBuilder(name, context);
	}

	public BasicComponentBuilder basicComponent() {
		return basicComponent(ModelBuilderUtil.randomName());
	}

	public ResourceDemandingSEFFBuilder seff(OperationSignature signature) {
		return new ResourceDemandingSEFFBuilder(signature, context);
	}

	public OperationSignatureBuilder signature(String name) {
		return new OperationSignatureBuilder(name, context);
	}

	public OperationSignatureBuilder signature() {
		return signature(ModelBuilderUtil.randomName());
	}

	public OperationInterfaceBuilder interfaze(String name) {
		return new OperationInterfaceBuilder(name, context);
	}

	public OperationInterfaceBuilder interfaze() {
		return interfaze(ModelBuilderUtil.randomName());
	}

	public Repository build() {
		return repository;
	}

}

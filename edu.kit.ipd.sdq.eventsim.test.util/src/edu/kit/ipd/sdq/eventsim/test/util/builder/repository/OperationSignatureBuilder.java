package edu.kit.ipd.sdq.eventsim.test.util.builder.repository;

import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;

public class OperationSignatureBuilder {

	private BuildingContext context;
	
	private OperationSignature signature;
	
	public OperationSignatureBuilder(String name, BuildingContext context) {
		this.context = context;
		signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setEntityName(name);
		context.add(signature);
	}
	
	public OperationSignatureBuilder returns(DataType type) {
		signature.setReturnType__OperationSignature(type);
		return this;
	}
	
	public void parameter(String name, DataType type) {
		Parameter p = RepositoryFactory.eINSTANCE.createParameter();
		p.setDataType__Parameter(type);
		p.setParameterName(name);
		p.setOperationSignature__Parameter(signature);
	}
	
	public OperationSignature buildIn(OperationInterface iface) {
		signature.setInterface__OperationSignature(iface);
		return signature;
	}
	
}

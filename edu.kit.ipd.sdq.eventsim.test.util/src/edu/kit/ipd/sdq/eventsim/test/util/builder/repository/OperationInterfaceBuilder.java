package edu.kit.ipd.sdq.eventsim.test.util.builder.repository;

import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;

public class OperationInterfaceBuilder {
	
	private OperationInterface iface;

	public OperationInterfaceBuilder(String name, BuildingContext context) {
		iface = RepositoryFactory.eINSTANCE.createOperationInterface();
		iface.setEntityName(name);
		context.add(iface);
	}

	public OperationInterface buildIn(Repository r) {
		iface.setRepository__Interface(r);
		return iface;
	}

}

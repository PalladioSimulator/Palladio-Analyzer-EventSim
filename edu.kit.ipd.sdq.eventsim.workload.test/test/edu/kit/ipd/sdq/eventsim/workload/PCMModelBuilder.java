package edu.kit.ipd.sdq.eventsim.workload;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcm.resourcetype.ResourcetypeFactory;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemFactory;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;

public class PCMModelBuilder {

	private Allocation allocationModel;
	private Repository repositoryModel;
	private ResourceEnvironment resourceModel;
	private System systemModel;
	private UsageModel usageModel;
	private ResourceRepository resourceRepository;

	public PCMModelBuilder() {
		allocationModel = AllocationFactory.eINSTANCE.createAllocation();
		repositoryModel = RepositoryFactory.eINSTANCE.createRepository();
		resourceModel = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
		systemModel = SystemFactory.eINSTANCE.createSystem();
		usageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
		resourceRepository = ResourcetypeFactory.eINSTANCE.createResourceRepository();
	}

	public PCMModelBuilder withUsageModel(UsageModel usageModel) {
		this.usageModel = usageModel;
		return this;
	}

	public PCMModel build() {
		return new PCMModel(allocationModel, repositoryModel, resourceModel, systemModel, usageModel,
				resourceRepository);
	}

}

package edu.kit.ipd.sdq.eventsim.test.util.builder.resourceenvironment;

import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;

public class ResourceEnvironmentBuilder {

	private BuildingContext context;
	
	private ResourceEnvironment resourceEnvironment;
	
	public ResourceEnvironmentBuilder(BuildingContext context) {
		this.context = context;
		this.resourceEnvironment = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
		context.add(resourceEnvironment);
	}

	public ResourceEnvironment build() {
		return resourceEnvironment;
	}

	public ResourceContainerBuilder newResourceContainer() {
		return new ResourceContainerBuilder(context);
	}

	
}

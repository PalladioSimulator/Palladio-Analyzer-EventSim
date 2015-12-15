package edu.kit.ipd.sdq.eventsim.test.util.builder.resourceenvironment;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;

public class ResourceContainerBuilder {

	private ResourceContainer container;
	
	public ResourceContainerBuilder(BuildingContext context) {
		this.container = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
	}

	public ResourceContainer buildIn(ResourceEnvironment re) {
		container.setResourceEnvironment_ResourceContainer(re);
		return container;
	}	

}

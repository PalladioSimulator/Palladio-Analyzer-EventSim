package edu.kit.ipd.sdq.eventsim.instrumentation.description.resource;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;

/**
 * Common interface for representations of resources.
 * 
 * @author Henning Schulz
 * 
 * @see ActiveResourceRep
 * @see PassiveResourceRep
 *
 */
public interface ResourceRepresentative extends Instrumentable {

//	boolean represents(ResourceContainer specification, ResourceType resourceType);
//
//	boolean represents(PassiveResource specification, AssemblyContext assCtx);
//
//	boolean represents(String firstSpec, String secondSpec);

	Class<? extends ResourceRepresentative> getResourceType();

}

package edu.kit.ipd.sdq.eventsim.instrumentation.description.resource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

/**
 * Represents an active resource by the ids of the corresponding
 * {@link ResourceContainer} and {@link ResourceType}.
 * 
 * @author Henning Schulz
 * 
 * @see ResourceRule
 *
 */
@XmlRootElement(name = "active-resource")
public class ActiveResourceRep implements ResourceRepresentative {

	private String specificationId;
	private String resourceTypeId;

	public ActiveResourceRep(ProcessingResourceSpecification specification) {
		this(specification.getResourceContainer_ProcessingResourceSpecification(),
				specification.getActiveResourceType_ActiveResourceSpecification());
	}
	
	public ActiveResourceRep(ResourceContainer specification, ResourceType resourceType) {
		this.specificationId = specification.getId();
		this.resourceTypeId = resourceType.getId();
	}

	public ActiveResourceRep(String specificationId, String resourceTypeId) {
		this.specificationId = specificationId;
		this.resourceTypeId = resourceTypeId;
	}

	public ActiveResourceRep() {
	}

	@XmlElement(name = "specification")
	public String getSpecificationId() {
		return specificationId;
	}

	public void setSpecificationId(String specificationId) {
		this.specificationId = specificationId;
	}

	@XmlElement(name = "resource-type")
	public String getResourceTypeId() {
		return resourceTypeId;
	}

	public void setResourceTypeId(String resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	@Override
	public boolean represents(ResourceContainer specification, ResourceType resourceType) {
		return this.specificationId.equals(specification.getId()) && this.resourceTypeId.equals(resourceType.getId());
	}

	@Override
	public boolean represents(PassiveResource specification, AssemblyContext assCtx) {
		return false;
	}

	@Override
	public boolean represents(String firstSpec, String secondSpec) {
		return this.specificationId.equals(firstSpec) && this.resourceTypeId.equals(secondSpec);
	}

	@Override
	public Class<? extends ResourceRepresentative> getResourceType() {
		return ActiveResourceRep.class;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((specificationId == null) ? 0 : specificationId.hashCode());
		result = prime * result + ((resourceTypeId == null) ? 0 : resourceTypeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!this.getClass().equals(obj.getClass()))
			return false;

		ActiveResourceRep other = (ActiveResourceRep) obj;

		if (this.specificationId == null) {
			if (other.specificationId != null)
				return false;
		} else {
			if (!this.specificationId.equals(other.specificationId))
				return false;
		}

		if (this.resourceTypeId == null) {
			if (other.resourceTypeId != null)
				return false;
		} else {
			if (!this.resourceTypeId.equals(other.resourceTypeId))
				return false;
		}

		return true;
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.description.resource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

/**
 * Represents a passive resource by the ids of the corresponding
 * {@link PassiveResource} and the {@link AssemblyContext}.
 * 
 * @author Henning Schulz
 * 
 * @see ResourceRule
 *
 */
@XmlRootElement(name = "passive-resource")
public class PassiveResourceRep implements ResourceRepresentative {

	private String specificationId;
	private String assemblyContextId;

	public PassiveResourceRep(PassiveResource specification, AssemblyContext assCtx) {
		this.specificationId = specification.getId();
		this.assemblyContextId = assCtx.getId();
	}

	public PassiveResourceRep(String specificationId, String assemblyContextId) {
		this.specificationId = specificationId;
		this.assemblyContextId = assemblyContextId;
	}

	public PassiveResourceRep() {
	}

	@XmlElement(name = "specification")
	public String getSpecificationId() {
		return specificationId;
	}

	public void setSpecificationId(String specificationId) {
		this.specificationId = specificationId;
	}

	@XmlElement(name = "assembly-context")
	public String getAssemblyContextId() {
		return assemblyContextId;
	}

	public void setAssemblyContextId(String assemblyContextId) {
		this.assemblyContextId = assemblyContextId;
	}

	@Override
	public boolean represents(ResourceContainer specification, ResourceType resourceType) {
		return false;
	}

	@Override
	public boolean represents(PassiveResource specification, AssemblyContext assCtx) {
		return this.specificationId.equals(specification.getId()) && this.assemblyContextId.equals(assCtx.getId());
	}

	@Override
	public boolean represents(String firstSpec, String secondSpec) {
		return this.specificationId.equals(firstSpec) && this.assemblyContextId.equals(secondSpec);
	}

	@Override
	public Class<? extends ResourceRepresentative> getResourceType() {
		return PassiveResourceRep.class;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((specificationId == null) ? 0 : specificationId.hashCode());
		result = prime * result + ((assemblyContextId == null) ? 0 : assemblyContextId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!this.getClass().equals(obj.getClass()))
			return false;

		PassiveResourceRep other = (PassiveResourceRep) obj;

		if (this.specificationId == null) {
			if (other.specificationId != null)
				return false;
		} else {
			if (!this.specificationId.equals(other.specificationId))
				return false;
		}

		if (this.assemblyContextId == null) {
			if (other.assemblyContextId != null)
				return false;
		} else {
			if (!this.assemblyContextId.equals(other.assemblyContextId))
				return false;
		}

		return true;
	}

}

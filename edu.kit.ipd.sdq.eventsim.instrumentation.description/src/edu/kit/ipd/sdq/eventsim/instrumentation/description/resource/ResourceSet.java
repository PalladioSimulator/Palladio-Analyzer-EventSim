package edu.kit.ipd.sdq.eventsim.instrumentation.description.resource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableSet;

/**
 * A set of resources, implicitly defined by all resources of a type (active or
 * passive) and a set of restrictions.
 * 
 * @author Henning Schulz
 *
 * @param <R>
 *            the type of resources ({@link ActiveResourceRep} or
 *            {@link PassiveResourceRep})
 */
@XmlRootElement(name = "resource-set")
public class ResourceSet<R extends ResourceRepresentative> extends InstrumentableSet<R> {

	private Class<R> resourceType;

	public ResourceSet(Class<R> resourceType) {
		this.resourceType = resourceType;
	}

	public ResourceSet() {
	}

	@XmlElement(name = "resource-type")
	public Class<R> getResourceType() {
		return resourceType;
	}

	public void setResourceType(Class<R> resourceType) {
		this.resourceType = resourceType;
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.description.resource;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;

/**
 * An {@link InstrumentationRule} for resources.
 * 
 * @author Henning Schulz
 *
 * @param <R>
 *            the type of resources (either {@link ActiveResourceRep} or
 *            {@link PassiveResourceRep})
 */
@XmlRootElement(name = "resource-rule")
public class ResourceRule<R extends ResourceRepresentative> extends SetBasedInstrumentationRule<R, R> {

	private ResourceSet<R> resourceSet;

	public ResourceRule(Class<R> resourceType) {
		resourceSet = new ResourceSet<>(resourceType);
		setName(resourceType.getSimpleName());
	}

	public ResourceRule() {
	}

	@XmlElement(name = "resource-set")
	public ResourceSet<R> getResourceSet() {
		return resourceSet;
	}

	public void setResourceSet(ResourceSet<R> resources) {
		this.resourceSet = resources;

		if (getName() == null) {
			setName(resources.getResourceType().getSimpleName());
		}
	}

	@Override
	public boolean affects(Instrumentable instrumentable) {
		if (!resourceSet.getResourceType().isAssignableFrom(instrumentable.getClass())) {
			return false;
		}

		@SuppressWarnings("unchecked")
		R resource = (R) instrumentable;

		return resourceSet.contains(resource);
	}

	@Override
	public Class<R> getProbedType() {
		return getResourceSet().getResourceType();
	}

	@Override
	public Class<R> getInstrumentableType() {
		return getResourceSet().getResourceType();
	}

	@Override
	public void addRestriction(InstrumentableRestriction<R> restriction) {
		if (restriction != null)
			resourceSet.addRestriction(restriction);
	}

	@Override
	public void removeRestriction(InstrumentableRestriction<R> restriction) {
		resourceSet.removeRestriction(restriction);
	}

	@Override
	public List<InstrumentableRestriction<R>> getRestrictions() {
		return resourceSet.getRestrictions();
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.resource;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;

/**
 * A restriction for resources that exclude all resources except for one.
 * 
 * @author Henning Schulz
 *
 * @param <R>
 *            the type of the resource ({@link ActiveResourceRep} or
 *            {@link PassiveResourceRep})
 */
@Restriction(name = "Restriction to a Single Resource", instrumentableType = ResourceRepresentative.class, converter = SingleResourceRestrictionConverter.class)
public class SingleResourceRestriction<R extends ResourceRepresentative> implements InstrumentableRestriction<R> {

	private R resource;

	public SingleResourceRestriction(R resource) {
		this.resource = resource;
	}

	public SingleResourceRestriction() {
	}

	public R getResource() {
		return resource;
	}

	public void setResource(R resource) {
		this.resource = resource;
	}

	@Override
	public boolean exclude(R resource) {
		return this.resource.equals(resource);
	}

	@Override
	public String getHint() {
		return "Single " + (resource.getResourceType().equals(ActiveResourceRep.class) ? "Active" : "Passive")
				+ " Resource";
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.resource;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.AssemblyContextRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.AssemblyContextRestrictionConverter;

@Restriction(name = "Restriction to an Assembly Context", instrumentableType = PassiveResourceRep.class, converter = AssemblyContextRestrictionConverter.class)
public class ResourceAssemblyContextRestriction extends AssemblyContextRestriction<PassiveResourceRep> {

	public ResourceAssemblyContextRestriction(String assemblyContextId) {
		super(assemblyContextId);
	}

	public ResourceAssemblyContextRestriction(AssemblyContext context) {
		super(context);
	}

	public ResourceAssemblyContextRestriction() {
		super();
	}

	@Override
	public boolean exclude(PassiveResourceRep resource) {
		return !resource.getAssemblyContextId().equals(getAssemblyContextId());
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.resource.FindAllPassiveResources;
import edu.kit.ipd.sdq.eventsim.command.resource.PassiveResourceContext;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.SingleElementsRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUI;

@RestrictionUI(restrictionType = ResourceAssemblyContextRestriction.class)
public class ResourceAssemblyContextRestrictionUI
		extends SingleElementsRestrictionUI<PassiveResourceRep, ResourceAssemblyContextRestriction, AssemblyContext> {

	private List<PassiveResourceRep> resources;

	private ResourceAssemblyContextRestriction restriction;

	@Override
	protected void initialize(ResourceAssemblyContextRestriction restriction) {
		this.restriction = restriction;
	}

	@Override
	protected ResourceAssemblyContextRestriction createNewRestriction() {
		return new ResourceAssemblyContextRestriction();
	}

	@Override
	protected String getInitallySelectedEntityId() {
		return restriction.getAssemblyContextId();
	}

	@Override
	protected void setIdToRestriction(String id) {
		restriction.setAssemblyContextId(id);
	}

	@Override
	protected List<AssemblyContext> getAllEntities() {
		PCMModelCommandExecutor executor = new PCMModelCommandExecutor(
				InstrumentationDescriptionEditor.getActive().getPcm());

		List<PassiveResourceContext> resourceContexts = executor.execute(new FindAllPassiveResources());
		resources = resourceContexts.stream()
				.map(res -> new PassiveResourceRep(res.getResource(), res.getAssemblyContext()))
				.collect(Collectors.toList());
		Set<AssemblyContext> contexts = resourceContexts.stream().map(res -> res.getAssemblyContext())
				.collect(Collectors.toSet());
		return new ArrayList<>(contexts);
	}

	@Override
	protected List<PassiveResourceRep> getInstrumentablesForEntity(AssemblyContext assemblyContext) {
		return resources.stream().filter(r -> r.getAssemblyContextId().equals(assemblyContext.getId()))
				.collect(Collectors.toList());
	}

}

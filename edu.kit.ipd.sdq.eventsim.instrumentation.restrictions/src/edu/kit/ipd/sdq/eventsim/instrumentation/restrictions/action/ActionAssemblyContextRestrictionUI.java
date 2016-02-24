package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.action.FindAllActionsByType;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.SingleElementsRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUI;

@RestrictionUI(restrictionType = ActionAssemblyContextRestriction.class)
public class ActionAssemblyContextRestrictionUI<A extends AbstractAction> extends
		SingleElementsRestrictionUI<ActionRepresentative<? extends A>, ActionAssemblyContextRestriction<A>, AssemblyContext> {

	private List<ActionRepresentative<? extends A>> actions;

	private ActionAssemblyContextRestriction<A> restriction;

	@Override
	protected void initialize(ActionAssemblyContextRestriction<A> restriction) {
		this.restriction = restriction;
	}

	@Override
	protected ActionAssemblyContextRestriction<A> createNewRestriction() {
		return new ActionAssemblyContextRestriction<>();
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
		@SuppressWarnings("unchecked")
		ActionRule<A> rule = (ActionRule<A>) InstrumentationDescriptionEditor.getActive().getActiveRule();
		actions = executor.execute(new FindAllActionsByType<>(rule.getActionType())).stream()
				.map(c -> new ActionRepresentative<>(c.getAction(), c.getAllocationContext(), c.getAssemblyContext()))
				.collect(Collectors.toList());

		Set<AssemblyContext> contexts = actions.stream().map(a -> a.getAssemblyContext()).collect(Collectors.toSet());
		return new ArrayList<>(contexts);
	}

	@Override
	protected List<ActionRepresentative<? extends A>> getInstrumentablesForEntity(AssemblyContext assemblyContext) {
		return actions.stream().filter(a -> a.getAssemblyContext().equals(assemblyContext))
				.collect(Collectors.toList());
	}

}

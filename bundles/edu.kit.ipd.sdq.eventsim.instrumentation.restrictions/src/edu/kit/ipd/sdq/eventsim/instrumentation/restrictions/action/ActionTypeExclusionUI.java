package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.action.FindAllActionsByType;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.SingleElementsRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.ActionTypeRepository;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUI;

@RestrictionUI(restrictionType = ActionTypeExclusion.class)
public class ActionTypeExclusionUI extends
		SingleElementsRestrictionUI<ActionRepresentative, ActionTypeExclusion, Class<? extends AbstractAction>> {

	private ActionTypeExclusion exclusion;
	private ActionRule rule;
	private List<ActionRepresentative> actions;

	@Override
	protected void initialize(ActionTypeExclusion restriction) {
		this.exclusion = restriction;
		this.rule = (ActionRule) InstrumentationDescriptionEditor.getActive().getActiveRule();
	}

	@Override
	protected ActionTypeExclusion createNewRestriction() {
		return new ActionTypeExclusion();
	}

	@Override
	protected String getInitallySelectedEntityId() {
		return exclusion.getExcludedType() == null ? null : exclusion.getExcludedType().getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setIdToRestriction(String className) {
		try {
			exclusion.setExcludedType((Class<? extends AbstractAction>) Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected List<ActionRepresentative> getInstrumentablesForEntity(Class<? extends AbstractAction> clazz) {
		// Return all actions NOT of the passed type
		return actions.stream().filter(a -> !clazz.isAssignableFrom(a.getActionType())).collect(Collectors.toList());
	}

	@Override
	protected String elementToName(Class<? extends AbstractAction> element) {
		return element.getSimpleName();
	}

	@Override
	protected String elementToID(Class<? extends AbstractAction> element) {
		return element.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Class<? extends AbstractAction>> getAllEntities() {
		// Needed later on;
		// Not in initialize() in order to ensure that a PCM is selected
		PCMModelCommandExecutor executor = new PCMModelCommandExecutor(
				InstrumentationDescriptionEditor.getActive().getPcm());
		this.actions = executor.execute(new FindAllActionsByType<>(rule.getActionType())).stream()
				.map(c -> new ActionRepresentative(c.getAction(), c.getAllocationContext(), c.getAssemblyContext()))
				.collect(Collectors.toList());

		// Retrieve all action types which are subtypes of A
		return ActionTypeRepository.getAllActionTypes().stream().filter(c -> rule.getActionType().isAssignableFrom(c))
				.map(c -> (Class<? extends AbstractAction>) c).collect(Collectors.toList());
	}

	@Override
	protected String getDescriptionMessage() {
		return "Please select the type of actions that should be excluded.";
	}

}

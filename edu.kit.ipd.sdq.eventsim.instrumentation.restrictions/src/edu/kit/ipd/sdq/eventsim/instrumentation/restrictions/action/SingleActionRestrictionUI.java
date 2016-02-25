package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.action.ActionContext;
import edu.kit.ipd.sdq.eventsim.command.action.FindAllActionsByType;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.SingleElementsRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUI;

/**
 * A UI for the {@link SingleActionRestriction}. It requires a repository model.
 * If no such model is references by the instrumentation description, a new one
 * has to be chosen by the user.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the type of the action
 */
@RestrictionUI(restrictionType = SingleActionRestriction.class)
public class SingleActionRestrictionUI<A extends AbstractAction>
		extends SingleElementsRestrictionUI<ActionRepresentative, SingleActionRestriction<A>, A> {

	private List<ActionRepresentative> actions;
	private SingleActionRestriction<A> restriction;

	@Override
	protected void initialize(SingleActionRestriction<A> restriction) {
		this.restriction = restriction;
	}

	@Override
	protected SingleActionRestriction<A> createNewRestriction() {
		ActionRule rule = (ActionRule) InstrumentationDescriptionEditor.getActive().getActiveRule();
		SingleActionRestriction<A> restriction = new SingleActionRestriction<>();
		restriction.setActionType((Class<A>) rule.getActionType());
		return restriction;
	}

	@Override
	protected String getInitallySelectedEntityId() {
		return restriction.getActionId();
	}

	@Override
	protected void setIdToRestriction(String id) {
		restriction.setActionId(id);
	}

	@Override
	protected List<ActionRepresentative> getInstrumentablesForEntity(A action) {
		return actions.stream().filter(a -> a.getRepresentedAction().equals(action)).collect(Collectors.toList());
	}

	@Override
	protected List<A> getAllEntities() {
		PCMModelCommandExecutor executor = new PCMModelCommandExecutor(
				InstrumentationDescriptionEditor.getActive().getPcm());
		List<ActionContext<A>> actionContexts = executor
				.execute(new FindAllActionsByType<>(restriction.getActionType()));
		actions = actionContexts.stream()
				.map(c -> new ActionRepresentative(c.getAction(), c.getAllocationContext(), c.getAssemblyContext()))
				.collect(Collectors.toList());
		return actionContexts.stream().map(c -> c.getAction()).collect(Collectors.toList());
	}

}

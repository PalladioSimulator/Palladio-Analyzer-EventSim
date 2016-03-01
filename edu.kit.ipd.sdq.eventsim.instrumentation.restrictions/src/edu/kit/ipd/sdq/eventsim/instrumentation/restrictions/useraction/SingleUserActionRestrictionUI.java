package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.useraction;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.useraction.FindAllUserActionsByType;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.SingleElementsRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUI;

@RestrictionUI(restrictionType = SingleUserActionRestriction.class)
public class SingleUserActionRestrictionUI<A extends AbstractUserAction>
		extends SingleElementsRestrictionUI<UserActionRepresentative, SingleUserActionRestriction<A>, A> {

	private SingleUserActionRestriction<A> restriction;

	@SuppressWarnings("unchecked")
	@Override
	protected void initialize(SingleUserActionRestriction<A> restriction) {
		this.restriction = restriction;

		if (restriction.getUserActionType() == null) {
			UserActionRule rule = (UserActionRule) InstrumentationDescriptionEditor.getActive().getActiveRule();
			restriction.setUserActionType((Class<A>) rule.getUserActionType());
		}
	}

	@Override
	protected SingleUserActionRestriction<A> createNewRestriction() {
		return new SingleUserActionRestriction<>();
	}

	@Override
	protected String getInitallySelectedEntityId() {
		return restriction.getUserActionId();
	}

	@Override
	protected void setIdToRestriction(String id) {
		restriction.setUserActionId(id);
	}

	@Override
	protected List<A> getAllEntities() {
		PCMModelCommandExecutor executor = new PCMModelCommandExecutor(
				InstrumentationDescriptionEditor.getActive().getPcm());
		return executor.execute(new FindAllUserActionsByType<>(restriction.getUserActionType()));
	}

	@Override
	protected List<UserActionRepresentative> getInstrumentablesForEntity(A action) {
		List<UserActionRepresentative> result = new ArrayList<>();
		result.add(new UserActionRepresentative(action));
		return result;
	}

	@Override
	protected String elementToName(A element) {
		return element.getEntityName() + " (" + element.getId() + ")";
	}

	@Override
	protected String elementToID(A element) {
		return element.getId();
	}

	@Override
	protected String getDescriptionMessage() {
		return "Please select the user action you want to restrict to.";
	}

}

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
		extends SingleElementsRestrictionUI<UserActionRepresentative<? extends A>, SingleUserActionRestriction<A>, A> {

	private SingleUserActionRestriction<A> restriction;

	@Override
	protected void initialize(SingleUserActionRestriction<A> restriction) {
		this.restriction = restriction;

		if (restriction.getUserActionType() == null) {
			@SuppressWarnings("unchecked")
			UserActionRule<A> rule = (UserActionRule<A>) InstrumentationDescriptionEditor.getActive().getActiveRule();
			restriction.setUserActionType(rule.getUserActionType());
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
	protected List<UserActionRepresentative<? extends A>> getInstrumentablesForEntity(A action) {
		List<UserActionRepresentative<? extends A>> result = new ArrayList<>();
		result.add(new UserActionRepresentative<>(action));
		return result;
	}

}

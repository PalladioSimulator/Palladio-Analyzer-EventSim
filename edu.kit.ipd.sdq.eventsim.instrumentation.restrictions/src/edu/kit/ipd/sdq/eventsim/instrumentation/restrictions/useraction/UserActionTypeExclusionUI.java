package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.useraction;

import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.useraction.FindAllUserActionsByType;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.SingleElementsRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.UserActionTypeRepository;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUI;

@RestrictionUI(restrictionType = UserActionTypeExclusion.class)
public class UserActionTypeExclusionUI extends
		SingleElementsRestrictionUI<UserActionRepresentative, UserActionTypeExclusion, Class<? extends AbstractUserAction>> {

	private UserActionTypeExclusion exclusion;
	private UserActionRule rule;
	private List<UserActionRepresentative> userActions;

	@Override
	protected void initialize(UserActionTypeExclusion restriction) {
		this.exclusion = restriction;
		this.rule = (UserActionRule) InstrumentationDescriptionEditor.getActive().getActiveRule();
	}

	@Override
	protected UserActionTypeExclusion createNewRestriction() {
		return new UserActionTypeExclusion();
	}

	@Override
	protected String getInitallySelectedEntityId() {
		return exclusion.getExcludedType() == null ? null : exclusion.getExcludedType().getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setIdToRestriction(String className) {
		try {
			exclusion.setExcludedType((Class<? extends AbstractUserAction>) Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected List<UserActionRepresentative> getInstrumentablesForEntity(Class<? extends AbstractUserAction> clazz) {
		// Return all actions NOT of the passed type
		return userActions.stream().filter(a -> !clazz.isAssignableFrom(a.getRepresentedUserAction().getClass()))
				.collect(Collectors.toList());
	}

	@Override
	protected String elementToName(Class<? extends AbstractUserAction> element) {
		return element.getSimpleName();
	}

	@Override
	protected String elementToID(Class<? extends AbstractUserAction> element) {
		return element.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Class<? extends AbstractUserAction>> getAllEntities() {
		// Needed later on;
		// Not in initialize() in order to ensure that a PCM is selected
		PCMModelCommandExecutor executor = new PCMModelCommandExecutor(
				InstrumentationDescriptionEditor.getActive().getPcm());
		this.userActions = executor.execute(new FindAllUserActionsByType<>(rule.getUserActionType())).stream()
				.map(a -> new UserActionRepresentative(a)).collect(Collectors.toList());

		// Retrieve all action types which are subtypes of A
		return UserActionTypeRepository.getAllUserActionTypes().stream()
				.filter(c -> rule.getUserActionType().isAssignableFrom(c))
				.map(c -> (Class<? extends AbstractUserAction>) c).collect(Collectors.toList());
	}

	@Override
	protected String getDescriptionMessage() {
		return "Please select the type of user actions that should be excluded.";
	}

}

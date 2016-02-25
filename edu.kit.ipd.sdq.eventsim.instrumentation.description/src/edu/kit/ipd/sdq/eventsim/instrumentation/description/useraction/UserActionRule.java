package edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;

/**
 * An {@code InstrumentationRule} for subtypes of {@link AbstractUserAction}.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the user action type
 * @see SetBasedInstrumentationRule
 * @see UserActionRepresentative
 */
@XmlRootElement(name = "user-action-rule")
public class UserActionRule extends SetBasedInstrumentationRule<AbstractUserAction, UserActionRepresentative> {

	private UserActionSet userActionSet;

	public UserActionRule() {
	}

	public UserActionRule(Class<? extends AbstractUserAction> actionType) {
		userActionSet = new UserActionSet(actionType);
		setName(actionType.getSimpleName());
	}

	public Class<? extends AbstractUserAction> getUserActionType() {
		return userActionSet == null ? null : userActionSet.getUserActionType();
	}

	@XmlElement(name = "user-action-set")
	public UserActionSet getUserActionSet() {
		return userActionSet;
	}

	public void setUserActionSet(UserActionSet actions) {
		this.userActionSet = actions;

		if (getName() == null) {
			setName(actions.getUserActionType().getSimpleName());
		}
	}

	@Override
	public boolean affects(Instrumentable instrumentable) {
		if (!(instrumentable instanceof UserActionRepresentative)) {
			return false;
		}

		UserActionRepresentative action = (UserActionRepresentative) instrumentable;
		if (!userActionSet.getUserActionType().isAssignableFrom(action.getRepresentedUserAction().getClass())) {
			return false;
		}

		return userActionSet.contains(action);
	}

	@Override
	public Class<? extends AbstractUserAction> getProbedType() {
		return getUserActionType();
	}

	@Override
	public Class<UserActionRepresentative> getInstrumentableType() {
		return UserActionRepresentative.class;
	}

	@Override
	public void addRestriction(InstrumentableRestriction<UserActionRepresentative> restriction) {
		if (restriction != null)
			userActionSet.addRestriction(restriction);
	}

	@Override
	public void removeRestriction(InstrumentableRestriction<UserActionRepresentative> restriction) {
		userActionSet.removeRestriction(restriction);
	}

	@Override
	public List<InstrumentableRestriction<UserActionRepresentative>> getRestrictions() {
		return userActionSet.getRestrictions();
	}

}

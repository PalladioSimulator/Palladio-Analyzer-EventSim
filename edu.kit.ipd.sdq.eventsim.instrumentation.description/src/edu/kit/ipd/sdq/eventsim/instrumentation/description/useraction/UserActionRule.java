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
public class UserActionRule<A extends AbstractUserAction>
		extends SetBasedInstrumentationRule<A, UserActionRepresentative<? extends A>> {

	private UserActionSet<A> userActionSet;

	public UserActionRule() {
	}

	public UserActionRule(Class<A> actionType) {
		userActionSet = new UserActionSet<>(actionType);
		setName(actionType.getSimpleName());
	}

	public Class<A> getUserActionType() {
		return userActionSet == null ? null : userActionSet.getUserActionType();
	}

	@XmlElement(name = "user-action-set")
	public UserActionSet<A> getUserActionSet() {
		return userActionSet;
	}

	public void setUserActionSet(UserActionSet<A> actions) {
		this.userActionSet = actions;

		if (getName() == null) {
			setName(actions.getUserActionType().getSimpleName());
		}
	}

	@Override
	public boolean affects(Instrumentable instrumentable) {
		if (!(instrumentable instanceof UserActionRepresentative<?>)) {
			return false;
		}

		UserActionRepresentative<?> action = (UserActionRepresentative<?>) instrumentable;
		if (!userActionSet.getUserActionType().isAssignableFrom(action.getRepresentedUserAction().getClass())) {
			return false;
		}

		@SuppressWarnings("unchecked")
		UserActionRepresentative<? extends A> typedAction = (UserActionRepresentative<? extends A>) action;

		return userActionSet.contains(typedAction);
	}

	@Override
	public Class<A> getProbedType() {
		return getUserActionType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<UserActionRepresentative<? extends A>> getInstrumentableType() {
		Class<?> type = UserActionRepresentative.class;
		return (Class<UserActionRepresentative<? extends A>>) type;
	}

	@Override
	public void addRestriction(InstrumentableRestriction<UserActionRepresentative<? extends A>> restriction) {
		if (restriction != null)
			userActionSet.addRestriction(restriction);
	}

	@Override
	public void removeRestriction(InstrumentableRestriction<UserActionRepresentative<? extends A>> restriction) {
		userActionSet.removeRestriction(restriction);
	}

	@Override
	public List<InstrumentableRestriction<UserActionRepresentative<? extends A>>> getRestrictions() {
		return userActionSet.getRestrictions();
	}

}

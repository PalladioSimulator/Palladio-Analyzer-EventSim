package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.useraction;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;

/**
 * A specific restriction for user actions excluding all user actions except for
 * one. This user action is specified by the corresponding id.<br>
 * 
 * Note that only one of this restriction per instrumentation rule should be
 * introduced, since two different of them would produce an empty set.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the type of the user action
 */
@Restriction(name = "Restriction to a Single User Action", instrumentableType = UserActionRepresentative.class, converter = SingleUserActionRestrictionConverter.class)
public class SingleUserActionRestriction<A extends AbstractUserAction>
		implements InstrumentableRestriction<UserActionRepresentative> {

	private String userActionId;
	private Class<A> userActionType;

	public SingleUserActionRestriction(A userAction) {
		this.userActionId = userAction.getId();
	}

	public SingleUserActionRestriction() {
	}

	public SingleUserActionRestriction(String userActionId, Class<A> userActionType) {
		this.userActionId = userActionId;
		this.userActionType = userActionType;
	}

	@Override
	public boolean exclude(UserActionRepresentative action) {
		return !this.userActionId.equals(action.getRepresentedUserAction().getId());
	}

	@Override
	public String getHint() {
		return "Single User Action: " + userActionId;
	}

	public String getUserActionId() {
		return userActionId;
	}

	public void setUserActionId(String userActionId) {
		this.userActionId = userActionId;
	}

	public Class<A> getUserActionType() {
		return userActionType;
	}

	public void setUserActionType(Class<A> userActionType) {
		this.userActionType = userActionType;
	}

}

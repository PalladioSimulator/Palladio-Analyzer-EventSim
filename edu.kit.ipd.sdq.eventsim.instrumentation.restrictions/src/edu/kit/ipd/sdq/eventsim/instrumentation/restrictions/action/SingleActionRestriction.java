package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;

/**
 * A specific restriction for actions excluding all actions except for one. This
 * action is specified by the corresponding id. Assembly and allocations
 * contexts are not considered.<br>
 * 
 * Note that only one of this restriction per instrumentation rule should be
 * introduced, since two different of them would produce an empty set.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the type of the action
 */
@Restriction(name = "Restriction to a Single Action", instrumentableType = ActionRepresentative.class, converter = SingleActionRestrictionConverter.class)
public class SingleActionRestriction<A extends AbstractAction>
		implements InstrumentableRestriction<ActionRepresentative<? extends A>> {

	private String actionId;
	private Class<A> actionType;

	@SuppressWarnings("unchecked")
	public SingleActionRestriction(A action) {
		this.actionId = action.getId();
		this.actionType = (Class<A>) action.getClass();
	}

	public SingleActionRestriction() {
	}

	public SingleActionRestriction(String actionId, Class<A> actionType) {
		this.actionId = actionId;
		this.actionType = actionType;
	}

	@Override
	public boolean exclude(ActionRepresentative<? extends A> action) {
		return !this.actionId.equals(action.getRepresentedAction().getId());
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public Class<A> getActionType() {
		return actionType;
	}

	public void setActionType(Class<A> actionType) {
		this.actionType = actionType;
	}

	@Override
	public String getHint() {
		return "Single Action: " + actionId;
	}

}

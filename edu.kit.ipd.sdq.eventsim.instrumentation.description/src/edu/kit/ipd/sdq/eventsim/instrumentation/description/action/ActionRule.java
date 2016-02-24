package edu.kit.ipd.sdq.eventsim.instrumentation.description.action;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;

/**
 * An {@code InstrumentationRule} for subtypes of {@link AbstractAction}.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the action type
 * @see SetBasedInstrumentationRule
 * @see ActionRepresentative
 */
@XmlRootElement(name = "action-rule")
public class ActionRule<A extends AbstractAction>
		extends SetBasedInstrumentationRule<A, ActionRepresentative<? extends A>> {

	private ActionSet<A> actionSet;

	public ActionRule() {
	}

	public ActionRule(Class<A> actionType) {
		actionSet = new ActionSet<>(actionType);
		setName(actionType.getSimpleName());
	}

	public Class<A> getActionType() {
		return actionSet == null ? null : actionSet.getActionType();
	}

	@XmlElement(name = "action-set")
	public ActionSet<A> getActionSet() {
		return actionSet;
	}

	public void setActionSet(ActionSet<A> actions) {
		this.actionSet = actions;

		if (getName() == null) {
			setName(actions.getActionType().getSimpleName());
		}
	}

	@Override
	public boolean affects(Instrumentable instrumentable) {
		if (!(instrumentable instanceof ActionRepresentative<?>)) {
			return false;
		}

		ActionRepresentative<?> action = (ActionRepresentative<?>) instrumentable;
		if (!actionSet.getActionType().isAssignableFrom(action.getRepresentedAction().getClass())) {
			return false;
		}

		@SuppressWarnings("unchecked")
		ActionRepresentative<? extends A> typedAction = (ActionRepresentative<? extends A>) action;

		return actionSet.contains(typedAction);
	}

	@Override
	public Class<A> getProbedType() {
		return getActionType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ActionRepresentative<? extends A>> getInstrumentableType() {
		Class<?> type = ActionRepresentative.class;
		return (Class<ActionRepresentative<? extends A>>) type;
	}

	@Override
	public void addRestriction(InstrumentableRestriction<ActionRepresentative<? extends A>> restriction) {
		if (restriction != null)
			actionSet.addRestriction(restriction);
	}

	@Override
	public void removeRestriction(InstrumentableRestriction<ActionRepresentative<? extends A>> restriction) {
		actionSet.removeRestriction(restriction);
	}

	@Override
	public List<InstrumentableRestriction<ActionRepresentative<? extends A>>> getRestrictions() {
		return actionSet.getRestrictions();
	}

}

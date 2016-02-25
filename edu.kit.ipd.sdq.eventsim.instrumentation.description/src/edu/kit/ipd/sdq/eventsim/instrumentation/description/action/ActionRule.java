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
public class ActionRule extends SetBasedInstrumentationRule<AbstractAction, ActionRepresentative> {

	private ActionSet actionSet;

	public ActionRule() {
	}

	public ActionRule(Class<? extends AbstractAction> actionType) {
		actionSet = new ActionSet(actionType);
		setName(actionType.getSimpleName());
	}

	public Class<? extends AbstractAction> getActionType() {
		return actionSet == null ? null : actionSet.getActionType();
	}

	@XmlElement(name = "action-set")
	public ActionSet getActionSet() {
		return actionSet;
	}

	public void setActionSet(ActionSet actions) {
		this.actionSet = actions;

		if (getName() == null) {
			setName(actions.getActionType().getSimpleName());
		}
	}

	@Override
	public boolean affects(Instrumentable instrumentable) {
		if (!(instrumentable instanceof ActionRepresentative)) {
			return false;
		}

		ActionRepresentative action = (ActionRepresentative) instrumentable;
		if (!actionSet.getActionType().isAssignableFrom(action.getRepresentedAction().getClass())) {
			return false;
		}

		return actionSet.contains(action);
	}

	@Override
	public Class<? extends AbstractAction> getProbedType() {
		return getActionType();
	}

	@Override
	public Class<ActionRepresentative> getInstrumentableType() {
		return ActionRepresentative.class;
	}

	@Override
	public void addRestriction(InstrumentableRestriction<ActionRepresentative> restriction) {
		if (restriction != null)
			actionSet.addRestriction(restriction);
	}

	@Override
	public void removeRestriction(InstrumentableRestriction<ActionRepresentative> restriction) {
		actionSet.removeRestriction(restriction);
	}

	@Override
	public List<InstrumentableRestriction<ActionRepresentative>> getRestrictions() {
		return actionSet.getRestrictions();
	}

}

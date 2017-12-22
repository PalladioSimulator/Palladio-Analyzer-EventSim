package edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction;

import javax.xml.bind.annotation.XmlElement;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableSet;

/**
 * Represents a set of {@code AbstractUserAction}s. The set is implicitly
 * defined by the supertype {@code A} of the contained actions and a set of
 * {@link InstrumentableRestriction}s.
 * 
 * @author Henning Schulz
 * 
 * @see UserActionRule
 */
public class UserActionSet extends InstrumentableSet<UserActionRepresentative> {

	private Class<? extends AbstractUserAction> actionType;

	public UserActionSet(Class<? extends AbstractUserAction> actionType) {
		this.actionType = actionType;
	}

	public UserActionSet() {
	}

	@XmlElement(name = "user-action-type")
	public Class<? extends AbstractUserAction> getUserActionType() {
		return actionType;
	}

	public void setUserActionType(Class<? extends AbstractUserAction> actionType) {
		this.actionType = actionType;
	}

}

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
 * @param <A>
 *            the type of the contained useractions
 * 
 * @see UserActionRule
 */
public class UserActionSet<A extends AbstractUserAction>
		extends InstrumentableSet<UserActionRepresentative<? extends A>> {

	private Class<A> actionType;

	public UserActionSet(Class<A> actionType) {
		this.actionType = actionType;
	}

	public UserActionSet() {
	}

	@XmlElement(name = "user-action-type")
	public Class<A> getUserActionType() {
		return actionType;
	}

	public void setUserActionType(Class<A> actionType) {
		this.actionType = actionType;
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.description.action;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableSet;

/**
 * Represents a set of {@code AbstractAction}s. The set is implicitly defined by
 * the supertype {@code A} of the contained actions and a set of
 * {@link InstrumentableRestriction}s.
 * 
 * @author Henning Schulz
 * 
 * @see ActionRule
 */
@XmlRootElement(name = "action-set")
public class ActionSet extends InstrumentableSet<ActionRepresentative> {

	private Class<? extends AbstractAction> actionType;

	public ActionSet(Class<? extends AbstractAction> actionType) {
		this.actionType = actionType;
	}

	public ActionSet() {
	}

	@XmlElement(name = "action-type")
	public Class<? extends AbstractAction> getActionType() {
		return actionType;
	}

	public void setActionType(Class<? extends AbstractAction> actionType) {
		this.actionType = actionType;
	}

}

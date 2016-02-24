package edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;

/**
 * Represents a user action (a subtype of {@link AbstractUserAction}).
 * Basically, it is a wrapper around a user action to fit the
 * {@link Instrumentable} interface. Instances of this class are generated at
 * runtime and are not stored.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the type of the represented user action
 * 
 * @see UserActionRule
 */
public class UserActionRepresentative<A extends AbstractUserAction> implements Instrumentable {

	private final A representedUserAction;

	public UserActionRepresentative(A representedUserAction) {
		super();
		this.representedUserAction = representedUserAction;
	}

	public A getRepresentedUserAction() {
		return representedUserAction;
	}

}

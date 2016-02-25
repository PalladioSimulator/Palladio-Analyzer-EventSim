package edu.kit.ipd.sdq.eventsim.instrumentation.builder;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;

/**
 * A builder for {@link UserActionRule}s.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the type of user actions
 */
public class UserActionRuleBuilder<A extends AbstractUserAction> extends AbstractSetBasedRuleBuilder<A>
		implements RestrictionBuilder<UserActionRepresentative, A> {

	private final Class<A> actionType;
	private final List<InstrumentableRestriction<UserActionRepresentative>> restrictions = new ArrayList<>();
	private final InstrumentationDescriptionBuilder idBuilder;

	public UserActionRuleBuilder(Class<A> actionType, InstrumentationDescriptionBuilder idBuilder) {
		this.actionType = actionType;
		this.idBuilder = idBuilder;
	}

	@Override
	public RestrictionBuilder<UserActionRepresentative, A> underRestriction(
			InstrumentableRestriction<UserActionRepresentative> restriction) {
		restrictions.add(restriction);
		return this;
	}

	@Override
	public InstrumentationDescriptionBuilder ruleDone() {
		UserActionRule rule = new UserActionRule(actionType);
		rule.getUserActionSet().getRestrictions().addAll(restrictions);
		rule.getProbes().addAll(getProbes());
		rule.getCalculators().addAll(getCalculators());

		idBuilder.registerRule(rule);
		return idBuilder;
	}

	@Override
	protected ProbeRepresentative createProbeRepresentative(String measuredProperty) {
		return new ProbeRepresentative(measuredProperty, actionType);
	}

}

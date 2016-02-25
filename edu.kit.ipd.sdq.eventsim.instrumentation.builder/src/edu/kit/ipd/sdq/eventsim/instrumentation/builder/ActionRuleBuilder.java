package edu.kit.ipd.sdq.eventsim.instrumentation.builder;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;

/**
 * A builder for {@link ActionRule}s.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the type of actions
 */
public class ActionRuleBuilder<A extends AbstractAction> extends AbstractSetBasedRuleBuilder<A>
		implements RestrictionBuilder<ActionRepresentative, A> {

	private final Class<A> actionType;
	private final List<InstrumentableRestriction<ActionRepresentative>> restrictions = new ArrayList<>();
	private final InstrumentationDescriptionBuilder idBuilder;

	public ActionRuleBuilder(Class<A> actionType, InstrumentationDescriptionBuilder idBuilder) {
		this.actionType = actionType;
		this.idBuilder = idBuilder;
	}

	@Override
	public RestrictionBuilder<ActionRepresentative, A> underRestriction(
			InstrumentableRestriction<ActionRepresentative> restriction) {
		restrictions.add(restriction);
		return this;
	}

	@Override
	public InstrumentationDescriptionBuilder ruleDone() {
		ActionRule rule = new ActionRule(actionType);
		rule.getActionSet().getRestrictions().addAll(restrictions);
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

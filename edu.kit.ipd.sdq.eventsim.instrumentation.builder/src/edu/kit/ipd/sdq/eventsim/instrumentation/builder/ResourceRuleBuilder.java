package edu.kit.ipd.sdq.eventsim.instrumentation.builder;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;

/**
 * A builder for {@link ResourceRule}s.
 * 
 * @author Henning Schulz
 *
 * @param <R>
 *            the type of resources
 */
public class ResourceRuleBuilder<R extends ResourceRepresentative> extends AbstractSetBasedRuleBuilder<R>
		implements RestrictionBuilder<R, R> {

	private final Class<R> resourceType;
	private final List<InstrumentableRestriction<R>> restrictions = new ArrayList<>();
	private final InstrumentationDescriptionBuilder idBuilder;

	public ResourceRuleBuilder(Class<R> resourceType, InstrumentationDescriptionBuilder idBuilder) {
		this.resourceType = resourceType;
		this.idBuilder = idBuilder;
	}

	@Override
	public InstrumentationDescriptionBuilder ruleDone() {
		ResourceRule<R> rule = new ResourceRule<>(resourceType);
		rule.getResourceSet().getRestrictions().addAll(restrictions);
		rule.getProbes().addAll(getProbes());
		rule.getCalculators().addAll(getCalculators());

		idBuilder.registerRule(rule);
		return idBuilder;
	}

	@Override
	protected ProbeRepresentative<R> createProbeRepresentative(String measuredProperty) {
		return new ProbeRepresentative<>(measuredProperty, resourceType);
	}

	@Override
	public RestrictionBuilder<R, R> underRestriction(InstrumentableRestriction<R> restriction) {
		restrictions.add(restriction);
		return this;
	}

}

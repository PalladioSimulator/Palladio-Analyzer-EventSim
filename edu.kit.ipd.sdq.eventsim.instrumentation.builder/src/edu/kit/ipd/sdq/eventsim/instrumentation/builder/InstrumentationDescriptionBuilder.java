package edu.kit.ipd.sdq.eventsim.instrumentation.builder;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.resource.SingleResourceRestriction;

/**
 * A builder for {@link InstrumentationDescription}s.
 * 
 * @author Henning Schulz
 *
 */
public class InstrumentationDescriptionBuilder {

	private final InstrumentationDescription description = new InstrumentationDescription();

	public <A extends AbstractUserAction> RestrictionBuilder<UserActionRepresentative<? extends A>, A> newUserActionRule(
			Class<A> actionType) {
		return new UserActionRuleBuilder<>(actionType, this);
	}

	public <A extends AbstractAction> RestrictionBuilder<ActionRepresentative<? extends A>, A> newActionRule(
			Class<A> actionType) {
		return new ActionRuleBuilder<>(actionType, this);
	}

	public <R extends ResourceRepresentative> RestrictionBuilder<R, R> newResourceRule(Class<R> resourceType) {
		return new ResourceRuleBuilder<>(resourceType, this);
	}

	public ProbeAndCalculatorBuilder<ActiveResourceRep> newSingleResourceRule(ResourceContainer specification,
			ResourceType resourceType) {
		ResourceRuleBuilder<ActiveResourceRep> builder = new ResourceRuleBuilder<>(ActiveResourceRep.class, this);
		builder.underRestriction(new SingleResourceRestriction<>(new ActiveResourceRep(specification, resourceType)));
		return builder;
	}

	public ProbeAndCalculatorBuilder<PassiveResourceRep> newSingleResourceRule(PassiveResource specification,
			AssemblyContext assCtx) {
		ResourceRuleBuilder<PassiveResourceRep> builder = new ResourceRuleBuilder<>(PassiveResourceRep.class, this);
		builder.underRestriction(new SingleResourceRestriction<>(new PassiveResourceRep(specification, assCtx)));
		return builder;
	}

	@SuppressWarnings("unchecked")
	public <R extends ResourceRepresentative> ProbeAndCalculatorBuilder<R> newSingleResourceRule(Class<R> resourceType,
			String firstSpec, String secondSpec) {
		if (resourceType.equals(ActiveResourceRep.class)) {
			ResourceRuleBuilder<ActiveResourceRep> builder = new ResourceRuleBuilder<>(ActiveResourceRep.class, this);
			builder.underRestriction(new SingleResourceRestriction<>(new ActiveResourceRep(firstSpec, secondSpec)));
			return (ProbeAndCalculatorBuilder<R>) builder;
		} else if (resourceType.equals(PassiveResourceRep.class)) {
			ResourceRuleBuilder<PassiveResourceRep> builder = new ResourceRuleBuilder<>(PassiveResourceRep.class, this);
			builder.underRestriction(new SingleResourceRestriction<>(new PassiveResourceRep(firstSpec, secondSpec)));
			return (ProbeAndCalculatorBuilder<R>) builder;
		} else {
			throw new IllegalArgumentException("Illegal resource type: " + resourceType);
		}
	}

	protected void registerRule(InstrumentationRule rule) {
		description.addRule(rule);
	}

	/**
	 * Creates and return the specified instrumentation description. Should be
	 * called after the last rule is finished.
	 * 
	 * @return the newly build instrumentation description
	 */
	public InstrumentationDescription build() {
		return description;
	}

}

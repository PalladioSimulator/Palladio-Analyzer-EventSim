package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;

/**
 * 
 * Central class representing an instrumentation description. It holds a list of
 * {@link InstrumentationRule}s. Each of them specify how to instrument on ore
 * several entities in a specific way. If a rule refers to a concrete Palladio
 * model, the corresponding URI should be set.
 * 
 * @author Henning Schulz
 * 
 * @see ActionRule
 * @see UserActionRule
 * @see ResourceRule
 *
 */
@XmlRootElement(namespace = "edu.kit.ipd.sdq.eventsim.instrumentation.description", name = "instrumentation-description")
public class InstrumentationDescription {

	public static final String UNDEFINED = "";

	private String allocationUri = UNDEFINED;
	private String repositoryUri = UNDEFINED;
	private String systemUri = UNDEFINED;
	private String usagemodelUri = UNDEFINED;
	private String resourceenvironmentUri = UNDEFINED;

	private List<InstrumentationRule> rules = new ArrayList<>();

	@XmlElementWrapper(name = "rules")
	@XmlElements({ @XmlElement(name = "action-rule", type = ActionRule.class),
			@XmlElement(name = "user-action-rule", type = UserActionRule.class),
			@XmlElement(name = "resource-rule", type = ResourceRule.class) })
	public List<InstrumentationRule> getRules() {
		return rules;
	}

	public void setRules(List<InstrumentationRule> rules) {
		this.rules = rules;
	}

	public List<ActionRule<?>> getActionRules() {
		return rules.stream().filter(rule -> (rule instanceof ActionRule)).map(rule -> (ActionRule<?>) rule)
				.collect(Collectors.toList());
	}

	public List<UserActionRule<?>> getUserActionRules() {
		return rules.stream().filter(rule -> (rule instanceof UserActionRule)).map(rule -> (UserActionRule<?>) rule)
				.collect(Collectors.toList());
	}

	public List<ResourceRule<?>> getResourceRules() {
		return rules.stream().filter(rule -> (rule instanceof ResourceRule<?>)).map(rule -> (ResourceRule<?>) rule)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <A extends AbstractAction> List<ActionRule<? super A>> getAffectingRules(ActionRepresentative<A> action) {
		return rules.stream().filter(rule -> rule.affects(action)).map(rule -> (ActionRule<? super A>) rule)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <A extends AbstractUserAction> List<UserActionRule<? super A>> getAffectingRules(
			UserActionRepresentative<A> userAction) {
		return rules.stream().filter(rule -> rule.affects(userAction)).map(rule -> (UserActionRule<? super A>) rule)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <R extends ResourceRepresentative> List<ResourceRule<R>> getAffectingRules(R resource) {
		return rules.stream()
				.filter(rule -> (rule instanceof ResourceRule)
						&& (((ResourceRule<?>) rule).getResourceSet().getResourceType() == resource.getResourceType())
						&& (((ResourceRule<R>) rule).getResourceSet().contains(resource)))
				.map(rule -> (ResourceRule<R>) rule).collect(Collectors.toList());
	}

	public void addRule(InstrumentationRule rule) {
		rules.add(rule);
	}

	public void removeRule(int index) {
		rules.remove(index);
	}

	@XmlElement(name = "allocation-model")
	public String getAllocationUri() {
		return allocationUri;
	}

	public void setAllocationUri(String allocationUri) {
		this.allocationUri = allocationUri;
	}

	@XmlElement(name = "repository-model")
	public String getRepositoryUri() {
		return repositoryUri;
	}

	public void setRepositoryUri(String repositoryUri) {
		this.repositoryUri = repositoryUri;
	}

	@XmlElement(name = "resourceenvironment-model")
	public String getResourceenvironmentUri() {
		return resourceenvironmentUri;
	}

	public void setResourceenvironmentUri(String resourceenvironmentUri) {
		this.resourceenvironmentUri = resourceenvironmentUri;
	}

	@XmlElement(name = "system-model")
	public String getSystemUri() {
		return systemUri;
	}

	public void setSystemUri(String systemUri) {
		this.systemUri = systemUri;
	}

	@XmlElement(name = "usage-model")
	public String getUsagemodelUri() {
		return usagemodelUri;
	}

	public void setUsagemodelUri(String usagemodelUri) {
		this.usagemodelUri = usagemodelUri;
	}

}

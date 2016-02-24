package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import org.eclipse.swt.widgets.Composite;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;

public class ResourceRuleUI<R extends ResourceRepresentative> extends SetBasedRuleUI<R, R, ResourceRule<R>> {

	public ResourceRuleUI(ResourceRule<R> rule, Composite parent, int style) {
		super(rule, parent, style);
	}

	@Override
	protected String getScopeTypeName() {
		if (ActiveResourceRep.class.isAssignableFrom(getRule().getInstrumentableType())) {
			return "Active Resources";
		} else {
			return "Passive Resources";
		}
	}

}

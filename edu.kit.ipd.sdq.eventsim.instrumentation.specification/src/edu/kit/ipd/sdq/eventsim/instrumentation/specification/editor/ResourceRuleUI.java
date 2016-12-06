package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import org.eclipse.swt.widgets.Composite;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.LinkingResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;

// TODO this whole class should be revised: so far many type parameters, but still hard-coded resource types!
public class ResourceRuleUI<R extends ResourceRepresentative> extends SetBasedRuleUI<R, R, ResourceRule<R>> {

    public ResourceRuleUI(ResourceRule<R> rule, Composite parent, int style) {
        super(rule, parent, style);
    }

    @Override
    protected String getScopeTypeName() {
        if (ActiveResourceRep.class.isAssignableFrom(getRule().getInstrumentableType())) {
            return "Active Resources";
        } else if (PassiveResourceRep.class.isAssignableFrom(getRule().getInstrumentableType())) {
            return "Passive Resources";
        } else if (LinkingResourceRep.class.isAssignableFrom(getRule().getInstrumentableType())) {
            return "Linking Resources";
        } else {
            throw new RuntimeException("Unknown resource type: " + getRule().getInstrumentableType());
        }
    }

}

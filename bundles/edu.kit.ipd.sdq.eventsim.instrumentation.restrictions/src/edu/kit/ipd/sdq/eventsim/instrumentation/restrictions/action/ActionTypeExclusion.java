package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.util.StringUtils;

@Restriction(name = "Exclusion of an Action Type", instrumentableType = ActionRepresentative.class, converter = ActionTypeExclusionConverter.class)
public class ActionTypeExclusion implements InstrumentableRestriction<ActionRepresentative> {

	private Class<? extends AbstractAction> excludedType;

	public ActionTypeExclusion(Class<? extends AbstractAction> excludedType) {
		this.excludedType = excludedType;
	}

	public ActionTypeExclusion() {
	}

	@Override
	public boolean exclude(ActionRepresentative action) {
		return excludedType.isAssignableFrom(action.getActionType());
	}

	@Override
	public String getHint() {
		return "Exclude " + StringUtils.pluralize(excludedType.getSimpleName());
	}

	public Class<? extends AbstractAction> getExcludedType() {
		return excludedType;
	}

	public void setExcludedType(Class<? extends AbstractAction> excludedType) {
		this.excludedType = excludedType;
	}

}

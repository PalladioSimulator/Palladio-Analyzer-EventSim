package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.useraction;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.util.StringUtils;

@Restriction(name = "Exclusion of a User Action Type", instrumentableType = UserActionRepresentative.class, converter = UserActionTypeExclusionConverter.class)
public class UserActionTypeExclusion implements InstrumentableRestriction<UserActionRepresentative> {

	private Class<? extends AbstractUserAction> excludedType;

	public UserActionTypeExclusion(Class<? extends AbstractUserAction> excludedType) {
		this.excludedType = excludedType;
	}

	public UserActionTypeExclusion() {
	}

	@Override
	public boolean exclude(UserActionRepresentative useraction) {
		return excludedType.isAssignableFrom(useraction.getRepresentedUserAction().getClass());
	}

	@Override
	public String getHint() {
		return "Exclude " + StringUtils.pluralize(excludedType.getSimpleName());
	}

	public Class<? extends AbstractUserAction> getExcludedType() {
		return excludedType;
	}

	public void setExcludedType(Class<? extends AbstractUserAction> excludedType) {
		this.excludedType = excludedType;
	}

}

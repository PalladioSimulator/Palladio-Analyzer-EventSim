package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import org.eclipse.swt.widgets.Composite;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.util.StringUtils;

public class UserActionRuleUI extends SetBasedRuleUI<AbstractUserAction, UserActionRepresentative, UserActionRule> {

	public UserActionRuleUI(UserActionRule rule, Composite parent, int style) {
		super(rule, parent, style);
	}

	@Override
	protected String getScopeTypeName() {
		return StringUtils.pluralize(getRule().getUserActionType().getSimpleName());
	}

}

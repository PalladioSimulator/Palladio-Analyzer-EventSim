package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import org.eclipse.swt.widgets.Composite;
import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.util.StringUtils;

public class ActionRuleUI<A extends AbstractAction>
		extends SetBasedRuleUI<A, ActionRepresentative<? extends A>, ActionRule<A>> {

	public ActionRuleUI(ActionRule<A> rule, Composite parent, int style) {
		super(rule, parent, style);
	}

	@Override
	protected String getScopeTypeName() {
		return StringUtils.pluralize(getRule().getActionType().getSimpleName());
	}

}

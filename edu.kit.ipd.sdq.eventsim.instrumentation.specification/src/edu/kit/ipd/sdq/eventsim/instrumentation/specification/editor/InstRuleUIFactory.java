package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;

public class InstRuleUIFactory {

	private static InstRuleUIFactory instance;

	public static InstRuleUIFactory getInstance() {
		if (instance == null) {
			instance = new InstRuleUIFactory();
		}

		return instance;
	}

	public InstrumentationRuleUI createView(InstrumentationRule rule, Composite parent, int style) {
		if (rule instanceof ActionRule) {
			return new ActionRuleUI<>((ActionRule<?>) rule, parent, style);
		} else if (rule instanceof UserActionRule) {
			return new UserActionRuleUI<>((UserActionRule<?>) rule, parent, style);
		} else if (rule instanceof ResourceRule) {
			return new ResourceRuleUI<>((ResourceRule<?>) rule, parent, style);
		}

		return new InstrumentationRuleUI() {

			@Override
			public void init() {
				Label label = new Label(parent, SWT.NONE);
				label.setText(
						"No view for instrumentation rule of type " + rule.getClass().getSimpleName() + " known!");
				label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			}

			@Override
			public InstrumentationRule getRule() {
				return null;
			}

			@Override
			public void dispose() {
			}

			@Override
			public void addDirtyListener(DirtyListener listener) {
			}
		};
	}

}

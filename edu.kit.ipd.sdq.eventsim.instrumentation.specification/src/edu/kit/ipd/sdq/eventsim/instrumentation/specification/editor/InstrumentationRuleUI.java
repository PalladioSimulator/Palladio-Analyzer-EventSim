package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationRule;

public interface InstrumentationRuleUI {

	InstrumentationRule getRule();

	void dispose();

	void init();

	void addDirtyListener(DirtyListener listener);

}

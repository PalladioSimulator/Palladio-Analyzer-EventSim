package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.action;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.AssemblyContextRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.AssemblyContextRestrictionConverter;

@Restriction(name = "Restriction to an Assembly Context", instrumentableType = ActionRepresentative.class, converter = AssemblyContextRestrictionConverter.class)
public class ActionAssemblyContextRestriction<A extends AbstractAction>
		extends AssemblyContextRestriction<ActionRepresentative<? extends A>> {

	public ActionAssemblyContextRestriction(String assemblyContextId) {
		super(assemblyContextId);
	}

	public ActionAssemblyContextRestriction(AssemblyContext context) {
		super(context);
	}

	public ActionAssemblyContextRestriction() {
		super();
	}

	@Override
	public boolean exclude(ActionRepresentative<? extends A> action) {
		return !action.getAssemblyContext().getId().equals(getAssemblyContextId());
	}

}

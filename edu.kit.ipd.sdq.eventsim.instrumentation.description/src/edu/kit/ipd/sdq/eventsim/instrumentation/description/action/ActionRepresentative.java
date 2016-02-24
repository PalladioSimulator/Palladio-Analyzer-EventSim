package edu.kit.ipd.sdq.eventsim.instrumentation.description.action;

import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;

/**
 * Represents an action (a subtype of {@link AbstractAction}) in an assembly and
 * an allocation context. Instances of this class are generated at runtime and
 * are not stored.
 * 
 * @author Henning Schulz
 *
 * @param <A>
 *            the type of the represented action action
 * 
 * @see ActionRule
 */
public class ActionRepresentative<A extends AbstractAction> implements Instrumentable {

	private final A representedAction;
	private final AllocationContext allocationContext;
	private final AssemblyContext assemblyContext;

	public ActionRepresentative(A representedAction, AllocationContext allocationContext,
			AssemblyContext assemblyContext) {
		this.representedAction = representedAction;
		this.allocationContext = allocationContext;
		this.assemblyContext = assemblyContext;
	}

	public A getRepresentedAction() {
		return representedAction;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends A> getActionType() {
		return (Class<? extends A>) representedAction.getClass();
	}

	public AllocationContext getAllocationContext() {
		return allocationContext;
	}

	public AssemblyContext getAssemblyContext() {
		return assemblyContext;
	}

}

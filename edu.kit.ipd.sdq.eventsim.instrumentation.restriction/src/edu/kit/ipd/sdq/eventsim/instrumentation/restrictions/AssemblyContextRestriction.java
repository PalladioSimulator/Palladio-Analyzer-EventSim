package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;

public abstract class AssemblyContextRestriction<I extends Instrumentable> implements InstrumentableRestriction<I> {

	private String assemblyContextId;

	public AssemblyContextRestriction(String assemblyContextId) {
		this.assemblyContextId = assemblyContextId;
	}

	public AssemblyContextRestriction(AssemblyContext context) {
		this.assemblyContextId = context.getId();
	}

	public AssemblyContextRestriction() {
	}

	@Override
	public String getHint() {
		return "Assembly Context: " + assemblyContextId;
	}

	public String getAssemblyContextId() {
		return assemblyContextId;
	}

	public void setAssemblyContextId(String assemblyContextId) {
		this.assemblyContextId = assemblyContextId;
	}

}

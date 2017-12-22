package edu.kit.ipd.sdq.eventsim.command.resource;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

public class PassiveResourceContext {

	private PassiveResource resource;

	private AssemblyContext assemblyContext;

	public PassiveResourceContext() {
	}

	public PassiveResourceContext(PassiveResource resource, AssemblyContext assemblyContext) {
		super();
		this.resource = resource;
		this.assemblyContext = assemblyContext;
	}

	public PassiveResource getResource() {
		return resource;
	}

	public void setResource(PassiveResource resource) {
		this.resource = resource;
	}

	public AssemblyContext getAssemblyContext() {
		return assemblyContext;
	}

	public void setAssemblyContext(AssemblyContext assemblyContext) {
		this.assemblyContext = assemblyContext;
	}

}

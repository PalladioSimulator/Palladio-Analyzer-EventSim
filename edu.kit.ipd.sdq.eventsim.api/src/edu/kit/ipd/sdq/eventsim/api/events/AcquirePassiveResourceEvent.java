package edu.kit.ipd.sdq.eventsim.api.events;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.middleware.events.AbstractSimulationEvent;

public class AcquirePassiveResourceEvent extends AbstractSimulationEvent {
	
	private IRequest request;

	private AssemblyContext assemblyContext;

	private PassiveResource passiveResouce;

	private int amount;

	public AcquirePassiveResourceEvent(IRequest request, AssemblyContext assemblyContext,
			PassiveResource passiveResouce, int amount) {
		this.request = request;
		this.assemblyContext = assemblyContext;
		this.passiveResouce = passiveResouce;
		this.amount = amount;
	}

	public AcquirePassiveResourceEvent(IRequest request, AssemblyContext assemblyContext,
			PassiveResource passiveResouce) {
		this(request, assemblyContext, passiveResouce, 1);
	}

	public IRequest getRequest() {
		return request;
	}

	public AssemblyContext getAssemblyContext() {
		return assemblyContext;
	}

	public PassiveResource getPassiveResouce() {
		return passiveResouce;
	}

	public int getAmount() {
		return amount;
	}

}

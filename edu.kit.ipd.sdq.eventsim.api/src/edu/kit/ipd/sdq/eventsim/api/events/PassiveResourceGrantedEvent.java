//package edu.kit.ipd.sdq.eventsim.api.events;
//
//import org.palladiosimulator.pcm.core.composition.AssemblyContext;
//import org.palladiosimulator.pcm.repository.PassiveResource;
//
//import edu.kit.ipd.sdq.eventsim.api.IRequest;
//import edu.kit.ipd.sdq.eventsim.middleware.events.AbstractSimulationEvent;
//import edu.kit.ipd.sdq.eventsim.middleware.events.SimulationEvent;
//
//public class PassiveResourceGrantedEvent extends AbstractSimulationEvent {
//
//	private IRequest request;
//
//	private AssemblyContext assemblyContext;
//
//	private PassiveResource passiveResouce;
//
//	public PassiveResourceGrantedEvent(IRequest request, AssemblyContext assemblyContext,
//			PassiveResource passiveResouce) {
//		this.request = request;
//		this.assemblyContext = assemblyContext;
//		this.passiveResouce = passiveResouce;
//	}
//
//	public IRequest getRequest() {
//		return request;
//	}
//	
//	public AssemblyContext getAssemblyContext() {
//		return assemblyContext;
//	}
//	
//	public PassiveResource getPassiveResouce() {
//		return passiveResouce;
//	}
//	
//}

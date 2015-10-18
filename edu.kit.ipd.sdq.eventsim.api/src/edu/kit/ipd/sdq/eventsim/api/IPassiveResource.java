package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

/**
 * Represents a passive resource simulation component which can be aquired in a
 * specific amount.
 * 
 * TODO (SimComp) Introduce passive resource simulation events
 * 
 * @author Christoph Föhrdes
 * 
 */
public interface IPassiveResource {

	/**
	 * Aquires a specific amount of this passive resource
	 * 
	 * @param request
	 * @param ctx
	 * @param passiveResouce
	 * @param num
	 * @return
	 */
	public boolean acquire(IRequest request, AssemblyContext ctx, PassiveResource passiveResouce, int num);

	/**
	 * Releases a specific amount of this passive resource
	 * 
	 * @param request
	 * @param ctx
	 * @param passiveResouce
	 * @param num
	 */
	public void release(IRequest request, AssemblyContext ctx, PassiveResource passiveResouce, int num);

}

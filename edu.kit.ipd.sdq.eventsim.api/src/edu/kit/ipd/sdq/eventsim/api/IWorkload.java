package edu.kit.ipd.sdq.eventsim.api;

/**
 * Represents a workload simulation component which generates calls to services
 * of an {@link ISystem} simulation component.
 * 
 * @author Christoph Föhrdes
 */
public interface IWorkload {

	/**
	 * Starts the workload generation.
	 */
	public void generate();
	
}

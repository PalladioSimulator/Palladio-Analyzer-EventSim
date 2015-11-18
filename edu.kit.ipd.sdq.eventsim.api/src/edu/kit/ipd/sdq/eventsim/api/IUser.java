package edu.kit.ipd.sdq.eventsim.api;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;

/**
 * Represents a system user which is spawned by an {@link IWorkload} to perform calls to an {@link ISystem}
 * 
 * @author Christoph Föhrdes
 */
public interface IUser {

	/**
	 * A unique identifier for the user instance.
	 * 
	 * @return A unique identifier
	 */
	public String getId();
	
	public StackContext getStochasticExpressionContext();
	
}

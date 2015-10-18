package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

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
	
	public void onSystemCall(SystemCallListener callback);
	
	public interface SystemCallListener {
		
		void call(IUser user, EntryLevelSystemCall call);
		
	}
	
}

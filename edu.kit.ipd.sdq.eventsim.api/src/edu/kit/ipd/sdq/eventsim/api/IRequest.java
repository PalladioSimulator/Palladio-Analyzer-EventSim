package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.seff.AbstractAction;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;

/**
 * Represents a request processed in a system simulation component.
 * 
 * @author Christoph Föhrdes
 */
public interface IRequest {

	public long getId();

	/**
	 * Returns the user who initiated the request.
	 * 
	 * @return The creator of the request
	 */
	public IUser getUser();

	public void activate();

	// TODO remove dependency upon abstract sim engine
	public void passivate(AbstractSimEventDelegator<?> activationEvent);
	
	public IRequest getParent();
	
	public AbstractAction getCurrentPosition();

}

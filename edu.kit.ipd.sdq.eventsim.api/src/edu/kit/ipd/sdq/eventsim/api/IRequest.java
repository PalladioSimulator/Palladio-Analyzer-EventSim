package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.seff.AbstractAction;

/**
 * A system request represents a system call by a {@link IUser}.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 */
public interface IRequest {

    /**
     * @return the unique identifier of this user
     */
    long getId();

    /**
     * @return the user who initiated this request.
     */
    IUser getUser();

    IRequest getParent();

    AbstractAction getCurrentPosition();

}

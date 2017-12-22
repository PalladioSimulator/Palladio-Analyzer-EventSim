package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;

/**
 * A facade of the linking resource simulation module.
 * 
 * @author Philipp Merkle
 * 
 */
public interface ILinkingResource {

    /**
     * Simulates a network transfer between two resource containers.
     * 
     * @param request
     *            the demanding request
     * @param specification
     *            specification of the linking resource connecting the resource containers
     * @param absoluteDemand
     *            the resource demand
     * @param onServedCallback
     *            the callback to be invoked once the requested demand has been served
     */
    void consume(IRequest request, LinkingResource specification, double absoluteDemand, Procedure onServedCallback);

}

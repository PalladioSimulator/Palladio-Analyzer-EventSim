package edu.kit.ipd.sdq.eventsim.api;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

/**
 * The passive resource simulation module allows to acquire and release passive resources.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 * 
 */
public interface IPassiveResource {

    /**
     * Acquires the specified number of instance of a passive resource.
     * 
     * @param request
     *            the request acquiring the passive resource
     * @param ctx
     *            the passive resource's assembly context is required to uniquely identify the
     *            passive resource
     * @param passiveResouce
     *            the passive resource to be acquired
     * @param num
     *            the number of instances
     * @param onGrantedCallback
     *            the callback to be invoked once the demanded number of instances have been granted
     *            to the request
     * @return
     */
    void acquire(IRequest request, AssemblyContext ctx, PassiveResource passiveResouce, int num,
            Procedure onGrantedCallback);

    /**
     * Releases a specific amount of a passive resource.
     * 
     * @param request
     *            the request releasing the passive resource
     * @param ctx
     *            the passive resource's assembly context is required to uniquely identify the
     *            passive resource
     * @param passiveResouce
     *            the passive resource to be released
     * @param num
     *            the number of instances
     */
    void release(IRequest request, AssemblyContext ctx, PassiveResource passiveResouce, int num);

}

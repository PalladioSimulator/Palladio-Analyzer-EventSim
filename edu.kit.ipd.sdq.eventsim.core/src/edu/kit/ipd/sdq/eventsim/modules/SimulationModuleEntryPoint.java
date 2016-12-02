package edu.kit.ipd.sdq.eventsim.modules;

import edu.kit.ipd.sdq.eventsim.api.events.SimulationPrepareEvent;

/**
 * Marker interface to designate a class that needs to be instantiated before the simulation starts.
 * Once instantiated, that class usually registers for the {@link SimulationPrepareEvent} to do
 * initialization work.
 * 
 * @author Philipp Merkle
 *
 */
public interface SimulationModuleEntryPoint {

    // marker interface, left blank intentionally

}

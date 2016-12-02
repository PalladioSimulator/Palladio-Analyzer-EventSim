package edu.kit.ipd.sdq.eventsim.interpreter;

import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;

public interface DecoratingSimulationStrategy<A extends Entity, E extends EventSimEntity>
        extends SimulationStrategy<A, E> {

    public void decorate(SimulationStrategy<A, E> decorated);

}

package edu.kit.ipd.sdq.eventsim.interpreter;

import java.util.function.Consumer;

import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.api.Procedure;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;

public interface SimulationStrategy<A extends Entity, E extends EventSimEntity> {

    public void simulate(A action, E request, Consumer<Procedure> onFinishCallback);

}

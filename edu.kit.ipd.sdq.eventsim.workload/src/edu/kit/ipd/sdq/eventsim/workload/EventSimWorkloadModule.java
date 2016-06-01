package edu.kit.ipd.sdq.eventsim.workload;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.workload.generator.UserFactory;
import edu.kit.ipd.sdq.eventsim.workload.generator.WorkloadGeneratorFactory;

/**
 * An EventSim based workload simulation component implementation.
 * 
 * @author Philipp Merkle
 */
public class EventSimWorkloadModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(UserFactory.class));
        install(new FactoryModuleBuilder().build(WorkloadGeneratorFactory.class));

        bind(IWorkload.class).to(EventSimWorkloadModel.class);
    }

}

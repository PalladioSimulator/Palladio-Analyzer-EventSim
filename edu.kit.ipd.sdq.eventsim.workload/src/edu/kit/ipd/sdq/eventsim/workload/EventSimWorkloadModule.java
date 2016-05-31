package edu.kit.ipd.sdq.eventsim.workload;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.kit.ipd.sdq.eventsim.api.IWorkload;
import edu.kit.ipd.sdq.eventsim.workload.entities.User;
import edu.kit.ipd.sdq.eventsim.workload.generator.ClosedWorkloadGenerator;
import edu.kit.ipd.sdq.eventsim.workload.generator.OpenWorkloadGenerator;
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
        install(new FactoryModuleBuilder().implement(User.class, User.class).build(UserFactory.class));
        
        install(new FactoryModuleBuilder().implement(ClosedWorkloadGenerator.class, ClosedWorkloadGenerator.class)
                .implement(OpenWorkloadGenerator.class, OpenWorkloadGenerator.class)
                .build(WorkloadGeneratorFactory.class));

        // bind interfaces of provided services to their implementation
        bind(IWorkload.class).to(EventSimWorkloadModel.class);
    }

}

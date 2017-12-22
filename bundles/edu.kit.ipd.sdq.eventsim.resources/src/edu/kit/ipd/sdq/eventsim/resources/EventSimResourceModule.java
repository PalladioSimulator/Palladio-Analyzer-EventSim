package edu.kit.ipd.sdq.eventsim.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import de.uka.ipd.sdq.scheduler.ISchedulingFactory;
import de.uka.ipd.sdq.scheduler.SchedulerModel;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.ILinkingResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.SimulationModel;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimResourceFactory;

public class EventSimResourceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(SimResourceFactory.class));

        bind(IActiveResource.class).to(EventSimActiveResourceModel.class).asEagerSingleton();
        bind(IPassiveResource.class).to(EventSimPassiveResourceModel.class).asEagerSingleton();
        bind(ILinkingResource.class).to(EventSimLinkingResourceModel.class).asEagerSingleton();

        bind(SchedulerModel.class).to(SimulationModel.class).in(Singleton.class);
        bind(ISchedulingFactory.class).to(InjectableSchedulingFactory.class).in(Singleton.class);
    }

}

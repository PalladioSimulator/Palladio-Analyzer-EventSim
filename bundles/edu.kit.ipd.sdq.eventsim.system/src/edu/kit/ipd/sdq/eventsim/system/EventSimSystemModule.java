package edu.kit.ipd.sdq.eventsim.system;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.kit.ipd.sdq.eventsim.api.ISystem;
import edu.kit.ipd.sdq.eventsim.system.entities.RequestFactory;

public class EventSimSystemModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(RequestFactory.class));
        
        bind(ISystem.class).to(EventSimSystemModel.class);
    }
    
}

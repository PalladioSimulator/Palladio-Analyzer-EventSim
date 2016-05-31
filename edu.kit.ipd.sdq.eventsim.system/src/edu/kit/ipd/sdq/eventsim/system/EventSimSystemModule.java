package edu.kit.ipd.sdq.eventsim.system;

import com.google.inject.AbstractModule;

import edu.kit.ipd.sdq.eventsim.api.ISystem;

public class EventSimSystemModule extends AbstractModule {

    @Override
    protected void configure() {
        // bind interfaces of provided services to their implementation
        bind(ISystem.class).to(EventSimSystemModel.class);
    }
    
}

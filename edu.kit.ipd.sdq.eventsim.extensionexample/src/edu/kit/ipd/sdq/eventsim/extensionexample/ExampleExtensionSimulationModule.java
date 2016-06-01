package edu.kit.ipd.sdq.eventsim.extensionexample;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.kit.ipd.sdq.eventsim.system.entities.Request;
import edu.kit.ipd.sdq.eventsim.system.entities.RequestFactory;

public class ExampleExtensionSimulationModule extends AbstractModule {

    @Override
    protected void configure() {
        // force the existing System Simulation Module to work with our own ExtendedRequests
        install(new FactoryModuleBuilder().implement(Request.class, ExtendedRequest.class).build(RequestFactory.class));
    }

}

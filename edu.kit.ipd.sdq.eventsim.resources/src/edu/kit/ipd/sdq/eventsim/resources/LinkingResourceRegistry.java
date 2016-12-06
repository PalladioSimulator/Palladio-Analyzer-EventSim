package edu.kit.ipd.sdq.eventsim.resources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.resources.entities.SimLinkingResource;

@Singleton
public class LinkingResourceRegistry {

    // maps LinkingResource -> SimLinkingResource
    private Map<LinkingResource, SimLinkingResource> resourceMap;

    private List<Consumer<SimLinkingResource>> registrationListeners;

    @Inject
    private ResourceFactory resourceFactory;

    public LinkingResourceRegistry() {
        resourceMap = new HashMap<>();
        registrationListeners = new LinkedList<>();
    }

    public void addResourceRegistrationListener(Consumer<SimLinkingResource> listener) {
        registrationListeners.add(listener);
    }

    private void notifyRegistrationListeners(SimLinkingResource resource) {
        registrationListeners.forEach(listener -> listener.accept(resource));
    }

    public SimLinkingResource findOrCreateResource(LinkingResource specification) {
        if (!resourceMap.containsKey(specification)) {
            // create linking resource
            SimLinkingResource resource = resourceFactory.createLinkingResource(specification);

            // register the created passive resource
            resourceMap.put(specification, resource);

            notifyRegistrationListeners(resource);
        }
        return resourceMap.get(specification);
    }

}

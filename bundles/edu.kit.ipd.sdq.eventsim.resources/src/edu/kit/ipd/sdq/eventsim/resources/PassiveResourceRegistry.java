package edu.kit.ipd.sdq.eventsim.resources;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.resources.entities.SimPassiveResource;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

@Singleton
public class PassiveResourceRegistry {

    private static final Logger logger = Logger.getLogger(PassiveResourceRegistry.class);

    // maps (AssemblyContext ID, PassiveResource ID) -> SimPassiveResource
    private Map<String, SimPassiveResource> contextToResourceMap;

    private List<Consumer<SimPassiveResource>> registrationListeners;

    @Inject
    private ResourceFactory resourceFactory;

    public PassiveResourceRegistry() {
        contextToResourceMap = new HashMap<>();
        registrationListeners = new LinkedList<>();
    }

    public void addResourceRegistrationListener(Consumer<SimPassiveResource> listener) {
        registrationListeners.add(listener);
    }

    private void notifyRegistrationListeners(SimPassiveResource resource) {
        registrationListeners.forEach(listener -> listener.accept(resource));
    }

    /**
     * Finds the resource that has been registered for the specified type. If no resource of the
     * specified type can be found, the search continues with the parent resource container.
     * 
     * @param type
     *            the resource type
     * @return the resource of the specified type, if there is one; null else
     */
    public SimPassiveResource findOrCreateResource(PassiveResource specification, AssemblyContext assCtx) {
        if (!contextToResourceMap.containsKey(compoundKey(assCtx, specification))) {
            // create passive resource
            SimPassiveResource resource = resourceFactory.createPassiveResource(specification, assCtx);

            // register the created passive resource
            contextToResourceMap.put(compoundKey(assCtx, specification), resource);

            logger.info(String.format("Created passive resource %s in assembly context %s",
                    PCMEntityHelper.toString(specification), PCMEntityHelper.toString(assCtx)));

            notifyRegistrationListeners(resource);
        }
        return contextToResourceMap.get(compoundKey(assCtx, specification));
    }

    private String compoundKey(AssemblyContext specification, PassiveResource resource) {
        // TODO better use resource name "CPU", HDD, ... as second component!?
        return specification.getId() + resource.getId();
    }

}

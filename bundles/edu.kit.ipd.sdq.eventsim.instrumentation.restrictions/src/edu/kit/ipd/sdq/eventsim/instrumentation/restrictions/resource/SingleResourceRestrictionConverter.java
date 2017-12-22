package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions.resource;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.LinkingResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.AdaptedInstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.RestrictionConverter;

public class SingleResourceRestrictionConverter implements RestrictionConverter {

    @Override
    public AdaptedInstrumentableRestriction fromImplementation(InstrumentableRestriction<?> restriction) {
        SingleResourceRestriction<?> res = (SingleResourceRestriction<?>) restriction;
        AdaptedInstrumentableRestriction adapted = new AdaptedInstrumentableRestriction();
        adapted.setType(res.getClass());
        adapted.addElement("type", Class.class, res.getResource().getResourceType().toString());

        // TODO should not be hard-coded
        if (ActiveResourceRep.class.equals(res.getResource().getResourceType())) {
            ActiveResourceRep resource = (ActiveResourceRep) res.getResource();
            adapted.addElement("resource-container", ResourceContainer.class, resource.getResourceContainerId());
            adapted.addElement("resource-type", ResourceType.class, resource.getResourceTypeId());
        } else if (PassiveResourceRep.class.equals(res.getResource().getResourceType())) {
            PassiveResourceRep resource = (PassiveResourceRep) res.getResource();
            adapted.addElement("specification", PassiveResource.class, resource.getSpecificationId());
            adapted.addElement("assembly-context", AssemblyContext.class, resource.getAssemblyContextId());
        } else if (LinkingResourceRep.class.equals(res.getResource().getResourceType())) {
            LinkingResourceRep resource = (LinkingResourceRep) res.getResource();
            adapted.addElement("resource-id", LinkingResource.class, resource.getLinkingResourceId());
        } else {
            throw new RuntimeException("Unknown resource type: " + res.getResource().getResourceType());
        }

        return adapted;
    }

    @Override
    public InstrumentableRestriction<?> fromAdaption(AdaptedInstrumentableRestriction adapted) {
        String typeName = adapted.getValue("type", Class.class);

        // TODO should not be hard-coded
        if (ActiveResourceRep.class.toString().equals(typeName)) {
            ActiveResourceRep resource = new ActiveResourceRep(
                    adapted.getValue("specification", ResourceContainer.class),
                    adapted.getValue("resource-type", ResourceType.class));
            return new SingleResourceRestriction<>(resource);
        } else if (PassiveResourceRep.class.toString().equals(typeName)) {
            PassiveResourceRep resource = new PassiveResourceRep(
                    adapted.getValue("specification", PassiveResource.class),
                    adapted.getValue("assembly-context", AssemblyContext.class));
            return new SingleResourceRestriction<>(resource);
        } else if (LinkingResourceRep.class.toString().equals(typeName)) {
            LinkingResourceRep resource = new LinkingResourceRep(
                    adapted.getValue("resource-id", LinkingResource.class));
            return new SingleResourceRestriction<>(resource);
        } else {
            throw new RuntimeException("Unknown resource type: " + typeName);
        }
    }

}

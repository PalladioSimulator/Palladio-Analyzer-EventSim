package edu.kit.ipd.sdq.eventsim.instrumentation.description.resource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ResourceType;

/**
 * Represents an active resource by the ids of the corresponding {@link ResourceContainer} and
 * {@link ResourceType}.
 * 
 * @author Henning Schulz
 * 
 * @see ResourceRule
 *
 */
@XmlRootElement(name = "linking-resource")
public class LinkingResourceRep implements ResourceRepresentative {

    private String linkingResourceId;

    public LinkingResourceRep(LinkingResource resource) {
        this.linkingResourceId = resource.getId();
    }
    
    public LinkingResourceRep(String linkingResourceId) {
        this.linkingResourceId = linkingResourceId;
    }

    public LinkingResourceRep() {
    }

    @XmlElement(name = "resource-id")
    public String getLinkingResourceId() {
        return linkingResourceId;
    }

    public void setLinkingResourceId(String linkingResourceId) {
        this.linkingResourceId = linkingResourceId;
    }

//    @Override
//    public boolean represents(ResourceContainer resourceContainer, ResourceType resourceType) {
//        return this.resourceContainerId.equals(resourceContainer.getId())
//                && this.resourceTypeId.equals(resourceType.getId());
//    }
//
//    @Override
//    public boolean represents(PassiveResource specification, AssemblyContext assCtx) {
//        return false;
//    }
//
//    @Override
//    public boolean represents(String firstSpec, String secondSpec) {
//        return this.resourceContainerId.equals(firstSpec) && this.resourceTypeId.equals(secondSpec);
//    }

    @Override
    public Class<? extends ResourceRepresentative> getResourceType() {
        return LinkingResourceRep.class;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((linkingResourceId == null) ? 0 : linkingResourceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkingResourceRep other = (LinkingResourceRep) obj;
        if (linkingResourceId == null) {
            if (other.linkingResourceId != null)
                return false;
        } else if (!linkingResourceId.equals(other.linkingResourceId))
            return false;
        return true;
    }

}

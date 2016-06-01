package edu.kit.ipd.sdq.eventsim.system.entities;

import org.palladiosimulator.pcm.seff.ForkedBehaviour;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import edu.kit.ipd.sdq.eventsim.api.IUser;

public interface RequestFactory {

    Request createRequest(EntryLevelSystemCall call, IUser user);
    
    ForkedRequest createForkedRequest(ForkedBehaviour behaviour, boolean asynchronous, Request parent);
    
}

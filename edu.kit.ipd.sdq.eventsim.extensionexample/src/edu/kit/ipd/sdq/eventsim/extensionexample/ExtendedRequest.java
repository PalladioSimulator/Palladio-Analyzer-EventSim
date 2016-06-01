package edu.kit.ipd.sdq.eventsim.extensionexample;

import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.IUser;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

public class ExtendedRequest extends Request {

    private static int COUNTER = 0;

    private int count;

    @Inject
    public ExtendedRequest(ISimulationModel model, @Assisted EntryLevelSystemCall call, @Assisted IUser user) {
        super(model, call, user);
        count = COUNTER++;
        System.out.println("Constructed extended request #" + count);
    }

    public int getCounter() {
        return count;
    }

}

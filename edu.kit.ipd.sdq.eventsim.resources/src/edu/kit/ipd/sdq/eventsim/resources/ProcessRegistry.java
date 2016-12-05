package edu.kit.ipd.sdq.eventsim.resources;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.IRequest;
import edu.kit.ipd.sdq.eventsim.entities.EventSimEntity;
import edu.kit.ipd.sdq.eventsim.entities.IEntityListener;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimulatedProcess;

@Singleton
public class ProcessRegistry {

    private Map<IRequest, SimulatedProcess> requestToSimulatedProcessMap;

    @Inject
    private ISimulationModel model;

    public ProcessRegistry() {
        requestToSimulatedProcessMap = new WeakHashMap<>();
    }

    /**
     * Returns the simulated process that is used to schedule resource requests issued by this
     * Request on an active or passive resource.
     * 
     * @return the simulated process
     */
    public SimulatedProcess getOrCreateSimulatedProcess(IRequest request) {
        if (!requestToSimulatedProcessMap.containsKey(request)) {
            SimulatedProcess parent = null;
            if (request.getParent() != null) {
                parent = getOrCreateSimulatedProcess(request.getParent());
            }
            SimulatedProcess process = new SimulatedProcess(model, parent, request);

            // add listener for request finish
            EventSimEntity requestEntity = (EventSimEntity) request;
            requestEntity.addEntityListener(new RequestFinishedHandler(process));

            requestToSimulatedProcessMap.put(request, process);
        }
        return requestToSimulatedProcessMap.get(request);
    }

    /**
     * This handler reacts when the Request has been finished and informs the simulated process
     * about that.
     * 
     * @author Philipp Merkle
     */
    private class RequestFinishedHandler implements IEntityListener {

        private WeakReference<SimulatedProcess> process;

        public RequestFinishedHandler(SimulatedProcess process) {
            this.process = new WeakReference<SimulatedProcess>(process);
        }

        @Override
        public void enteredSystem() {
            // nothing to do
        }

        @Override
        public void leftSystem() {
            process.get().terminate();
            requestToSimulatedProcessMap.remove(process.get().getRequest());
        }

    }

}

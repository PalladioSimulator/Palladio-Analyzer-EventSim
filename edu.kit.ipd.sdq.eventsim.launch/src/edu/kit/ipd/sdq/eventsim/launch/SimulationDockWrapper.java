package edu.kit.ipd.sdq.eventsim.launch;

import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

import de.uka.ipd.sdq.codegen.simucontroller.SimuControllerPlugin;
import de.uka.ipd.sdq.codegen.simucontroller.dockmodel.DockModel;
import de.uka.ipd.sdq.simulation.IStatusObserver;

public class SimulationDockWrapper implements IStatusObserver {

	private static final String DOCK_IDLE_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/DOCK_IDLE";
	private static final String SIM_STOPPED_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/SIM_STOPPED";
	private static final String SIM_STARTED_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/SIM_STARTED";
	private static final String DOCK_BUSY_TOPIC = "de/uka/ipd/sdq/simucomframework/simucomdock/DOCK_BUSY";

	private EventAdmin eventAdmin;

	private final DockModel dock;

	private SimulationDockWrapper(DockModel dock) {
		this.dock = dock;
		this.eventAdmin = discoverEventAdmin();
	}

	public static SimulationDockWrapper getBestFreeDock() {
		// setup simulation dock (progress viewer)
		DockModel dock = null;
		try {
			dock = SimuControllerPlugin.getDockModel().getBestFreeDock();
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO
		}
		return new SimulationDockWrapper(dock);
	}

	private EventAdmin discoverEventAdmin() {
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference<EventAdmin> eventServiceRef = context.getServiceReference(EventAdmin.class);
		ServiceTracker<?, ?> eventService = new ServiceTracker<>(context, eventServiceRef, null);
		eventService.open();
		return (EventAdmin) eventService.getService();
	}

	private void sendEventToSimulationDock(String topic, DockModel dock) {
		Map<String, Object> properties = new Hashtable<>();
		properties.put("DOCK_ID", dock.getID());
		eventAdmin.sendEvent(new Event(topic, properties));
	}

	public void start() {
		sendEventToSimulationDock(DOCK_BUSY_TOPIC, dock);
		sendEventToSimulationDock(SIM_STARTED_TOPIC, dock);
	}
	
	public void stop() {
		sendEventToSimulationDock(SIM_STOPPED_TOPIC, dock);
		sendEventToSimulationDock(DOCK_IDLE_TOPIC, dock);
	}
	
	@Override
	public void updateStatus(int percentDone, double currentSimTime, long measurementsTaken) {
		dock.setMeasurementCount(measurementsTaken);
		dock.setPercentDone(percentDone);
		dock.setSimTime(currentSimTime);
	}

}

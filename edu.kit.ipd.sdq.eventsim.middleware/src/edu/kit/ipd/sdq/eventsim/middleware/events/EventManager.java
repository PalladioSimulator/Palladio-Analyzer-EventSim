package edu.kit.ipd.sdq.eventsim.middleware.events;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import edu.kit.ipd.sdq.eventsim.middleware.Activator;

/**
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 *
 */
public class EventManager {

	private static final Logger logger = Logger.getLogger(EventManager.class);
	
	private EventAdmin eventAdmin;
	
	private List<ServiceRegistration<?>> handlerRegistrations;
	
	public EventManager() {
		handlerRegistrations = new ArrayList<ServiceRegistration<?>>();
		
		// Prepare event admin service
		BundleContext bundleContext = Activator.getContext();
		ServiceReference<EventAdmin> eventAdminServiceReference = bundleContext.getServiceReference(EventAdmin.class);
		eventAdmin = bundleContext.getService(eventAdminServiceReference);
	}
	
	public void triggerEvent(SimulationEvent event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Event triggered (" + SimulationEvent.topicName(event.getClass()) + ")");
		}

		// we delegate the event to the OSGi event admin service
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(SimulationEvent.ENCAPSULATED_EVENT, event);
		eventAdmin.sendEvent(new Event(SimulationEvent.topicName(event.getClass()), properties));

	}

	public <T extends SimulationEvent> void registerEventHandler(Class<T> eventType, final IEventHandler<T> handler) {
		// we delegate the event handling to the OSGi event admin service
		BundleContext bundleContext = Activator.getContext();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(EventConstants.EVENT_TOPIC, SimulationEvent.topicName(eventType));
		ServiceRegistration<EventHandler> handlerService = bundleContext.registerService(EventHandler.class, new EventHandler() {

			@Override
			public void handleEvent(Event event) {
				// TODO get rid of cast?
				@SuppressWarnings("unchecked")
				T simulationEvent = (T) event.getProperty(SimulationEvent.ENCAPSULATED_EVENT);
				handler.handle(simulationEvent);
			}

		}, properties);

		// store service registration for later cleanup
		handlerRegistrations.add(handlerService);
	}
	
	public void unregisterAllEventHandlers() {
		for (ServiceRegistration<?> reg : handlerRegistrations) {
			reg.unregister();
		}
	}
	
}

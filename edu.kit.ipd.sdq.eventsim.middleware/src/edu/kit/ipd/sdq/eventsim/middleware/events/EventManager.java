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

import com.google.inject.Singleton;

import edu.kit.ipd.sdq.eventsim.api.events.IEventHandler;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationEvent;
import edu.kit.ipd.sdq.eventsim.middleware.Activator;

/**
 * Wraps the OSGi {@link EventAdmin} service for better type safety. {@link SimulationEvent}s and {@link IEventHandler}s
 * are strongly typed, whereas the classical way of using OSGi {@link Event}s (hidden by this wrapper) involves undesired
 * type casts.
 * 
 * @author Christoph Föhrdes
 * @author Philipp Merkle
 *
 */
@Singleton
public class EventManager {

	private static final Logger log = Logger.getLogger(EventManager.class);

	private EventAdmin eventAdmin;

	private List<ServiceRegistration<?>> handlerRegistrations;

	public EventManager() {
		handlerRegistrations = new ArrayList<ServiceRegistration<?>>();

		// discover event admin service
		BundleContext bundleContext = Activator.getContext();
		ServiceReference<EventAdmin> eventAdminServiceReference = bundleContext.getServiceReference(EventAdmin.class);
		eventAdmin = bundleContext.getService(eventAdminServiceReference);
	}

	/**
	 * Delivers the specified {@code event} to interested event handlers. Returns not until all interested event
	 * handlers processed the event completely (synchronous delivery).
	 * 
	 * @param event
	 *            the event to be delivered
	 */
	public void triggerEvent(SimulationEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("Event triggered (" + SimulationEvent.topicName(event.getClass()) + ")");
		}

		// we delegate the event to the OSGi event admin service
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(SimulationEvent.ENCAPSULATED_EVENT, event);
		properties.putAll(event.getProperties());
		eventAdmin.sendEvent(new Event(SimulationEvent.topicName(event.getClass()), properties));

	}

	/**
	 * Registers the specified handler with events of the specified type.
	 * 
	 * @param eventType
	 *            the type of events handled by the handler
	 * @param handler
	 *            the event handler
	 */
	public <T extends SimulationEvent> void registerEventHandler(Class<T> eventType, final IEventHandler<T> handler,
			String filter) {
		BundleContext bundleContext = Activator.getContext();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(EventConstants.EVENT_TOPIC, SimulationEvent.topicName(eventType));
		if (filter != null && !filter.isEmpty()) {
			properties.put(EventConstants.EVENT_FILTER, filter);
		}
		ServiceRegistration<EventHandler> handlerRegistration = bundleContext.registerService(EventHandler.class,
				event -> {
					@SuppressWarnings("unchecked")
					T encapsulatedEvent = (T) event.getProperty(SimulationEvent.ENCAPSULATED_EVENT);
					handler.handle(encapsulatedEvent);
				} , properties);

		// store service registration for later cleanup
		handlerRegistrations.add(handlerRegistration);
	}

	public void unregisterAllEventHandlers() {
		for (ServiceRegistration<?> reg : handlerRegistrations) {
			reg.unregister();
		}
	}

}

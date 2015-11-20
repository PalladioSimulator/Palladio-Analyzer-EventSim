package edu.kit.ipd.sdq.eventsim.middleware.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class AbstractComponentFacade implements ComponentFacade {

	private static final Logger log = Logger.getLogger(AbstractComponentFacade.class);
	
	private Map<Class<?>, RequiredRole<?>> requiredRoles;

	private Map<Class<?>, ProvidedRole<?>> providedRoles;

	public AbstractComponentFacade() {
		requiredRoles = new HashMap<>();
		providedRoles = new HashMap<>();
	}

	public <T> void require(Class<T> requiredType) {
		require(requiredType, null);
	}
	
	public <T> void require(Class<T> requiredType, WiringListener<T> listener) {
		requiredRoles.put(requiredType, new RequiredRole<>(requiredType, listener));
	}

	public <T> void provide(Class<T> providedType, T instance) {
		providedRoles.put(providedType, new ProvidedRole<T>(providedType, instance));
	}

	@Override
	public <T> RequiredRole<T> getRequiredRole(Class<T> type) {
		@SuppressWarnings("unchecked")
		RequiredRole<T> role = ((RequiredRole<T>) requiredRoles.get(type));
		if(role == null) {
			log.warn("This component does not require " + type);
		}
		return role;
	}

	@Override
	public <T> T getRequiredService(Class<T> type) {
		return getRequiredRole(type).getService();
	}

	@Override
	public <T> ProvidedRole<T> getProvidedRole(Class<T> type) {
		@SuppressWarnings("unchecked")
		ProvidedRole<T> role = ((ProvidedRole<T>) providedRoles.get(type));
		if(role == null) {
			log.warn("This component does not provide " + type);
		}
		return role;
	}

}

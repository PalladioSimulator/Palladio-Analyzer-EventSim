package edu.kit.ipd.sdq.eventsim.middleware.components;

public interface ComponentFacade {
	
	<T> RequiredRole<T> getRequiredRole(Class<T> type);
	
	<T> T getRequiredService(Class<T> type);

	<T> ProvidedRole<T> getProvidedRole(Class<T> type);
	
}

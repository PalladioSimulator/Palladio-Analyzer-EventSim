package edu.kit.ipd.sdq.eventsim.components;

public class ProvidedRole<T> {
	
	private Class<T> type;
	
	private T instance;
	
	public ProvidedRole(Class<T> providedType, T instance) {
		this.type = providedType;
		this.instance = instance;
	}
	
	public T getInstance() {
		return instance;
	}
	
	public Class<T> getType() {
		return type;
	}

}

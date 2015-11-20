package edu.kit.ipd.sdq.eventsim.middleware.components;

public interface WiringListener<T> {
	
	void notify(T bound);

}

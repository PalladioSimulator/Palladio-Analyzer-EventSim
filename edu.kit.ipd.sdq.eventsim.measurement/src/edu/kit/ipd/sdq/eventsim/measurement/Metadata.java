package edu.kit.ipd.sdq.eventsim.measurement;

public class Metadata {

	private String name;
	
	private Object value;

	public Metadata(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public Object getValue() {
		return value;
	}
	
}

package edu.kit.ipd.sdq.eventsim.measurement;

public class Metadata {

    public static final String NAME_PREFIX = "m.";
    
	private String name;
	
	private Object value;

	public Metadata(String name, Object value) {
		this.name = NAME_PREFIX + name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public Object getValue() {
		return value;
	}
	
}

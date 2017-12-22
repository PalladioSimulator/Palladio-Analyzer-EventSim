package edu.kit.ipd.sdq.eventsim.measurement;

public class Metadata {

    public static final String NAME_PREFIX = "m.";

    private String name;

    private Object value;

    private boolean factorial;

    public Metadata(String name, Object value, boolean factorial) {
        this.name = NAME_PREFIX + name;
        this.value = value;
        this.factorial = factorial;
    }

    public Metadata(String name, Object value) {
        this(name, value, true);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
    
    public boolean isFactorial() {
        return factorial;
    }

}

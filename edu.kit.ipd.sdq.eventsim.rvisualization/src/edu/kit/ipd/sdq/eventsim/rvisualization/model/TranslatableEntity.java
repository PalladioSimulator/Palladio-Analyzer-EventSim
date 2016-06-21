package edu.kit.ipd.sdq.eventsim.rvisualization.model;

public class TranslatableEntity {

    private String translation;
    
    private String name;

    public TranslatableEntity(String name, String translation) {
        this.name = name;
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }

    public String getName() {
        return name;
    }
    
}

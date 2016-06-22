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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TranslatableEntity other = (TranslatableEntity) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
}

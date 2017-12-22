package edu.kit.ipd.sdq.eventsim.rvisualization.model;

public class VariableBinding {

    private TranslatableEntity variable;
    
    private TranslatableEntity bindingType;

    public VariableBinding(TranslatableEntity variable, TranslatableEntity bindingType) {
        this.variable = variable;
        this.bindingType = bindingType;
    }
    
    public TranslatableEntity getVariable() {
        return variable;
    }
    
    public TranslatableEntity getBindingType() {
        return bindingType;
    }
    
}

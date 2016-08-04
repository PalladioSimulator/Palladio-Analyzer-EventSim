package edu.kit.ipd.sdq.eventsim.rvisualization.model;

public class VariableBinding {

    private String variable;
    
    private String bindingType;

    public VariableBinding(String variable, String bindingType) {
        this.variable = variable;
        this.bindingType = bindingType;
    }
    
    public String getVariable() {
        return variable;
    }
    
    public String getBindingType() {
        return bindingType;
    }
    
}

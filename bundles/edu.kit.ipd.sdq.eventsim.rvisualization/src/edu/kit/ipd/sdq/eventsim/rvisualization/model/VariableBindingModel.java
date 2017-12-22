package edu.kit.ipd.sdq.eventsim.rvisualization.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class VariableBindingModel {

    public static final String UNBOUND_VARIABLES_PROPERTY = "unboundVariables";

    public static final String VARIABLE_BINDINGS_PROPERTY = "variableBindings";

    public static final String AVAILABLE_BINDING_TYPES_PROPERTY = "availableBindingTypes";

    public static final String SELECTED_UNBOUND_VARIABLE_PROPERTY = "selectedUnboundVariable";

    public static final String SELECTED_VARIABLE_BINDING_PROPERTY = "selectedVariableBinding";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private List<TranslatableEntity> unboundVariables = new ArrayList<>();

    private List<VariableBinding> variableBindings = new ArrayList<>();

    private List<TranslatableEntity> availableBindingTypes = new ArrayList<>();

    private TranslatableEntity selectedUnboundVariable;

    private VariableBinding selectedVariableBinding;

    public VariableBindingModel() {
        // nothing to do
    }

    /**
     * Copy constructor.
     * 
     * @param bindingModel
     *            the original model to be copied
     */
    public VariableBindingModel(VariableBindingModel bindingModel) {
        unboundVariables = new ArrayList<>(bindingModel.getUnboundVariables());
        variableBindings = new ArrayList<>(bindingModel.getVariableBindings());
        availableBindingTypes = new ArrayList<>(bindingModel.getAvailableBindingTypes());
        selectedUnboundVariable = bindingModel.selectedUnboundVariable;
        selectedVariableBinding = bindingModel.selectedVariableBinding;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public List<TranslatableEntity> getUnboundVariables() {
        return unboundVariables;
    }

    public void setUnboundVariables(List<TranslatableEntity> unboundVariables) {
        List<TranslatableEntity> oldValue = this.unboundVariables;
        this.unboundVariables = unboundVariables;
        pcs.firePropertyChange(UNBOUND_VARIABLES_PROPERTY, oldValue, unboundVariables);
    }

    public boolean addUnboundVariable(TranslatableEntity unboundVariable) {
        List<TranslatableEntity> changedList = new ArrayList<>();
        changedList.addAll(unboundVariables);
        boolean added = changedList.add(unboundVariable);
        setUnboundVariables(changedList);
        return added;
    }

    public boolean removeUnboundVariable(TranslatableEntity unboundVariable) {
        List<TranslatableEntity> changedList = new ArrayList<>();
        changedList.addAll(unboundVariables);
        boolean removed = changedList.remove(unboundVariable);
        setUnboundVariables(changedList);
        return removed;
    }

    public List<VariableBinding> getVariableBindings() {
        return variableBindings;
    }

    public void setVariableBindings(List<VariableBinding> variableBindings) {
        List<VariableBinding> oldValue = this.variableBindings;
        this.variableBindings = variableBindings;
        pcs.firePropertyChange(VARIABLE_BINDINGS_PROPERTY, oldValue, variableBindings);
    }

    public boolean addVariableBinding(VariableBinding binding) {
        List<VariableBinding> changedList = new ArrayList<>();
        changedList.addAll(variableBindings);
        boolean added = changedList.add(binding);
        setVariableBindings(changedList);
        return added;
    }

    public boolean removeVariableBinding(VariableBinding binding) {
        List<VariableBinding> changedList = new ArrayList<>();
        changedList.addAll(variableBindings);
        boolean removed = changedList.remove(binding);
        setVariableBindings(changedList);
        return removed;
    }

    public List<TranslatableEntity> getAvailableBindingTypes() {
        return availableBindingTypes;
    }

    public void setAvailableBindingTypes(List<TranslatableEntity> availableBindingTypes) {
        List<TranslatableEntity> oldValue = this.availableBindingTypes;
        this.availableBindingTypes = availableBindingTypes;
        pcs.firePropertyChange(AVAILABLE_BINDING_TYPES_PROPERTY, oldValue, availableBindingTypes);
    }

    public boolean addAvailableBindingType(TranslatableEntity bindingType) {
        List<TranslatableEntity> changedList = new ArrayList<>();
        changedList.addAll(availableBindingTypes);
        boolean added = changedList.add(bindingType);
        setAvailableBindingTypes(changedList);
        return added;
    }

    public boolean removeAvailableBindingType(TranslatableEntity bindingType) {
        List<TranslatableEntity> changedList = new ArrayList<>();
        changedList.addAll(availableBindingTypes);
        boolean removed = changedList.remove(bindingType);
        setAvailableBindingTypes(changedList);
        return removed;
    }

    public TranslatableEntity getSelectedUnboundVariable() {
        return selectedUnboundVariable;
    }

    public void setSelectedUnboundVariable(TranslatableEntity selectedUnboundVariable) {
        TranslatableEntity oldValue = this.selectedUnboundVariable;
        this.selectedUnboundVariable = selectedUnboundVariable;
        pcs.firePropertyChange(SELECTED_UNBOUND_VARIABLE_PROPERTY, oldValue, selectedUnboundVariable);
    }

    public VariableBinding getSelectedVariableBinding() {
        return selectedVariableBinding;
    }

    public void setSelectedVariableBinding(VariableBinding selectedVariableBinding) {
        VariableBinding oldValue = this.selectedVariableBinding;
        this.selectedVariableBinding = selectedVariableBinding;
        pcs.firePropertyChange(SELECTED_VARIABLE_BINDING_PROPERTY, oldValue, selectedVariableBinding);
    }

}

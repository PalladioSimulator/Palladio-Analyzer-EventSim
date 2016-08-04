package edu.kit.ipd.sdq.eventsim.rvisualization.model.provider;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.VariableBinding;

public class VariableBindingValueProvider extends ObservableMapLabelProvider {

    public VariableBindingValueProvider(IObservableMap attributeMap) {
        super(attributeMap);
    }

    public VariableBindingValueProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);
    }

    @Override
    public String getText(Object element) {
        VariableBinding binding = (VariableBinding) element;
        return binding.getBindingType() + " -> " + binding.getVariable();
    }

}

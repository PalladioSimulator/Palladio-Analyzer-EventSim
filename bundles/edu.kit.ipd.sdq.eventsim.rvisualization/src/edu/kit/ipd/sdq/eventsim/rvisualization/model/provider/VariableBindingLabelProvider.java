package edu.kit.ipd.sdq.eventsim.rvisualization.model.provider;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.VariableBinding;

public class VariableBindingLabelProvider extends ObservableMapLabelProvider {

    public VariableBindingLabelProvider(IObservableMap attributeMap) {
        super(attributeMap);
    }

    public VariableBindingLabelProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);
    }

    @Override
    public String getText(Object element) {
        VariableBinding binding = (VariableBinding) element;
        return binding.getBindingType().getTranslation() + " -> " + binding.getVariable().getTranslation();
    }

}

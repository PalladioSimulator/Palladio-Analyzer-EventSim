package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;

public class EntityLabelProvider extends ObservableMapLabelProvider {

    public EntityLabelProvider(IObservableMap attributeMap) {
        super(attributeMap);
    }

    public EntityLabelProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);
    }

    @Override
    public String getText(Object element) {
        Entity entity = (Entity) element;
        return entity.getName() + " (ID: " + entity.getId() + ")";
    }

}

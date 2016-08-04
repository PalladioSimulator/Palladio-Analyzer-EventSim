package edu.kit.ipd.sdq.eventsim.rvisualization.model.provider;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;

public class TranslatableEntityLabelProvider extends ObservableMapLabelProvider {

    public TranslatableEntityLabelProvider(IObservableMap attributeMap) {
        super(attributeMap);
    }

    public TranslatableEntityLabelProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);
    }

    @Override
    public String getText(Object element) {
        TranslatableEntity entity = (TranslatableEntity) element;
        return entity.getTranslation();
    }

}

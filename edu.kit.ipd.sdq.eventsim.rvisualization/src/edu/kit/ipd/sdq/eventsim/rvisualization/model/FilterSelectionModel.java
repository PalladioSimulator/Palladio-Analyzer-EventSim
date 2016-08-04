package edu.kit.ipd.sdq.eventsim.rvisualization.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterSelectionModel {

    public static final String METRIC_PROPERTY = "metric";

    public static final String TRIGGER_TYPE_PROPERTY = "triggerType";

    public static final String TRIGGER_INSTANCE_PROPERTY = "triggerInstance";

    public static final String TRIGGER_INSTANCE_SELECTION_ENABLED = "triggerInstanceSelectionEnabled";

    public static final String ASSEMBLY_CONTEXT_PROPERTY = "assemblyContext";

    public static final String MEASURING_POINT_FROM_PROPERTY = "measuringPointFrom";

    public static final String MEASURING_POINT_TO_PROPERTY = "measuringPointTo";

    public static final String SIMULATION_TIME_LOWER_PROPERTY = "simulationTimeLower";

    public static final String SIMULATION_TIME_UPPER_PROPERTY = "simulationTimeUpper";

    public static final String DIAGRAM_TYPE_PROPERTY = "diagramType";

    public static final String METADATA_PROPERTY = "metadata";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private TranslatableEntity metric;

    private TranslatableEntity triggerType;

    private Entity triggerInstance;

    private boolean triggerInstanceSelectionEnabled;

    private Entity assemblyContext;

    private Entity measuringPointFrom;

    private Entity measuringPointTo;

    private int simulationTimeLower;

    private int simulationTimeUpper;

    private TranslatableEntity diagramType;

    private Map<TranslatableEntity, String> metadata;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public TranslatableEntity getMetric() {
        return metric;
    }

    public void setMetric(TranslatableEntity metric) {
        TranslatableEntity oldValue = this.metric;
        this.metric = metric;
        pcs.firePropertyChange(METRIC_PROPERTY, oldValue, metric);
    }

    public TranslatableEntity getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TranslatableEntity triggerType) {
        TranslatableEntity oldValue = this.triggerType;
        this.triggerType = triggerType;
        pcs.firePropertyChange(TRIGGER_TYPE_PROPERTY, oldValue, triggerType);
    }

    public Entity getTriggerInstance() {
        return triggerInstance;
    }

    public void setTriggerInstance(Entity triggerInstance) {
        Entity oldValue = this.triggerInstance;
        this.triggerInstance = triggerInstance;
        pcs.firePropertyChange(TRIGGER_INSTANCE_PROPERTY, oldValue, triggerInstance);
    }

    public boolean isTriggerInstanceSelectionEnabled() {
        return triggerInstanceSelectionEnabled;
    }

    public void setTriggerInstanceSelectionEnabled(boolean triggerInstanceSelectionEnabled) {
        boolean oldValue = this.triggerInstanceSelectionEnabled;
        this.triggerInstanceSelectionEnabled = triggerInstanceSelectionEnabled;
        pcs.firePropertyChange(TRIGGER_INSTANCE_SELECTION_ENABLED, oldValue, triggerInstanceSelectionEnabled);
    }

    public Entity getAssemblyContext() {
        return assemblyContext;
    }

    public void setAssemblyContext(Entity assemblyContext) {
        Entity oldValue = this.assemblyContext;
        this.assemblyContext = assemblyContext;
        pcs.firePropertyChange(ASSEMBLY_CONTEXT_PROPERTY, oldValue, assemblyContext);
    }

    public Entity getMeasuringPointFrom() {
        return measuringPointFrom;
    }

    public void setMeasuringPointFrom(Entity measuringPointFrom) {
        Entity oldValue = this.measuringPointFrom;
        this.measuringPointFrom = measuringPointFrom;
        pcs.firePropertyChange(MEASURING_POINT_FROM_PROPERTY, oldValue, measuringPointFrom);
    }

    public Entity getMeasuringPointTo() {
        return measuringPointTo;
    }

    public void setMeasuringPointTo(Entity measuringPointTo) {
        Entity oldValue = this.measuringPointTo;
        this.measuringPointTo = measuringPointTo;
        pcs.firePropertyChange(MEASURING_POINT_TO_PROPERTY, oldValue, measuringPointTo);
    }

    public int getSimulationTimeLower() {
        return simulationTimeLower;
    }

    public void setSimulationTimeLower(int simulationTimeLower) {
        int oldValue = this.simulationTimeLower;
        this.simulationTimeLower = simulationTimeLower;
        pcs.firePropertyChange(SIMULATION_TIME_LOWER_PROPERTY, oldValue, simulationTimeLower);
    }

    public int getSimulationTimeUpper() {
        return simulationTimeUpper;
    }

    public void setSimulationTimeUpper(int simulationTimeUpper) {
        int oldValue = this.simulationTimeUpper;
        this.simulationTimeUpper = simulationTimeUpper;
        pcs.firePropertyChange(SIMULATION_TIME_UPPER_PROPERTY, oldValue, simulationTimeUpper);
    }

    public TranslatableEntity getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(TranslatableEntity diagramType) {
        TranslatableEntity oldValue = this.diagramType;
        this.diagramType = diagramType;
        pcs.firePropertyChange(DIAGRAM_TYPE_PROPERTY, oldValue, diagramType);
    }

    public Map<TranslatableEntity, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<TranslatableEntity, String> metadata) {
        Map<TranslatableEntity, String> oldValue = this.metadata;
        this.metadata = metadata;
        pcs.firePropertyChange(METADATA_PROPERTY, oldValue, metadata);
    }

    public void clear() {
        setMetric(null);
        setTriggerType(null);
        setTriggerInstance(null);
        setTriggerInstanceSelectionEnabled(false);
        setAssemblyContext(null);
        setMeasuringPointFrom(null);
        setMeasuringPointTo(null);
        setSimulationTimeLower(0);
        setSimulationTimeUpper(0);
        setDiagramType(null);
        setMetadata(null);
    }

    public List<TranslatableEntity> getUnboundVariables(FilterModel model) {
        List<TranslatableEntity> unbound = new ArrayList<>();
        if (metric == null) {
            unbound.add(new TranslatableEntity("what", "Metric"));
        }
        if (triggerType == null) {
            unbound.add(new TranslatableEntity("who.type", "Trigger (Type)"));
        }
        if (assemblyContext == null) {
            unbound.add(new TranslatableEntity("assemblycontext.id", "Assembly Context (Identifier)"));
            unbound.add(new TranslatableEntity("assemblycontext.name", "Assembly Context (Name)"));
            unbound.add(new TranslatableEntity("assemblycontext.type", "Assembly Context (Type)"));
        }
        if (measuringPointFrom == null) {
            unbound.add(new TranslatableEntity("where.first.id", "First Measuring Point (Identifier)"));
            unbound.add(new TranslatableEntity("where.first.name", "First Measuring Point (Name)"));
            unbound.add(new TranslatableEntity("where.first.type", "First Measuring Point (Type)"));
        }
        if (measuringPointTo == null) {
            unbound.add(new TranslatableEntity("where.second.id", "Second Measuring Point (Identifier)"));
            unbound.add(new TranslatableEntity("where.second.name", "Second Measuring Point (Name)"));
            unbound.add(new TranslatableEntity("where.second.type", "Second Measuring Point (Type)"));
        }

        // add all unbound metadata types
        Set<TranslatableEntity> unboundMetadataTypes = new HashSet<>();
        unboundMetadataTypes.addAll(model.getMetadataTypes());
        if (metadata != null) {
            unboundMetadataTypes.removeAll(metadata.keySet());
        }
        unbound.addAll(unboundMetadataTypes);

        return unbound;
    }

}

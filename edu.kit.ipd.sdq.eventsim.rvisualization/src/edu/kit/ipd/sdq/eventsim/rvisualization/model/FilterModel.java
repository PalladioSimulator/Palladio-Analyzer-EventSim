package edu.kit.ipd.sdq.eventsim.rvisualization.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class FilterModel {

    public static final String SIMULATION_TIME_MAX_PROPERTY = "simulationTimeMax";

    public static final String SIMULATION_TIME_MIN_PROPERTY = "simulationTimeMin";

    public static final String ASSEMBLY_CONTEXTS_PROPERTY = "assemblyContexts";

    public static final String TRIGGER_INSTANCES_PROPERTY = "triggerInstances";

    public static final String TRIGGER_TYPES_PROPERTY = "triggerTypes";

    public static final String MEASURING_POINTS_TO_PROPERTY = "measuringPointsTo";

    public static final String MEASURING_POINTS_FROM_PROPERTY = "measuringPointsFrom";

    public static final String METRICS_PROPERTY = "metrics";
    
    public static final String DIAGRAM_TYPE_PROPERTY = "diagramTypes";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private List<TranslatableEntity> metrics;

    private List<TranslatableEntity> triggerTypes;

    private List<Entity> triggerInstances;

    private List<Entity> assemblyContexts;

    private List<Entity> measuringPointsFrom;

    private List<Entity> measuringPointsTo;

    private int simulationTimeMin;

    private int simulationTimeMax;
    
    private List<TranslatableEntity> diagramTypes;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public List<TranslatableEntity> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<TranslatableEntity> metrics) {
        if (metrics != null && metrics.isEmpty()) {
            metrics = null;
        }
        List<TranslatableEntity> oldValue = this.metrics;
        this.metrics = metrics;
        pcs.firePropertyChange(METRICS_PROPERTY, oldValue, metrics);
    }

    public List<Entity> getMeasuringPointsFrom() {
        return measuringPointsFrom;
    }

    public void setMeasuringPointsFrom(List<Entity> measuringPointsFrom) {
        if (measuringPointsFrom != null && measuringPointsFrom.isEmpty()) {
            measuringPointsFrom = null;
        }
        List<Entity> oldValue = this.measuringPointsFrom;
        this.measuringPointsFrom = measuringPointsFrom;
        pcs.firePropertyChange(MEASURING_POINTS_FROM_PROPERTY, oldValue, measuringPointsFrom);
    }

    public List<Entity> getMeasuringPointsTo() {
        return measuringPointsTo;
    }

    public void setMeasuringPointsTo(List<Entity> measuringPointsTo) {
        if (measuringPointsTo != null && measuringPointsTo.isEmpty()) {
            measuringPointsTo = null;
        }
        List<Entity> oldValue = this.measuringPointsTo;
        this.measuringPointsTo = measuringPointsTo;
        pcs.firePropertyChange(MEASURING_POINTS_TO_PROPERTY, oldValue, measuringPointsTo);
    }

    public List<TranslatableEntity> getTriggerTypes() {
        return triggerTypes;
    }

    public void setTriggerTypes(List<TranslatableEntity> triggerTypes) {
        if (triggerTypes != null && triggerTypes.isEmpty()) {
            triggerTypes = null;
        }
        List<TranslatableEntity> oldValue = this.triggerTypes;
        this.triggerTypes = triggerTypes;
        pcs.firePropertyChange(TRIGGER_TYPES_PROPERTY, oldValue, triggerTypes);
    }

    public List<Entity> getTriggerInstances() {
        return triggerInstances;
    }

    public void setTriggerInstances(List<Entity> triggerInstances) {
        if (triggerInstances != null && triggerInstances.isEmpty()) {
            triggerInstances = null;
        }
        List<Entity> oldValue = this.triggerInstances;
        this.triggerInstances = triggerInstances;
        pcs.firePropertyChange(TRIGGER_INSTANCES_PROPERTY, oldValue, triggerInstances);
    }

    public List<Entity> getAssemblyContexts() {
        return assemblyContexts;
    }

    public void setAssemblyContexts(List<Entity> assemblyContexts) {
        if (assemblyContexts != null && assemblyContexts.isEmpty()) {
            assemblyContexts = null;
        }
        List<Entity> oldValue = this.assemblyContexts;
        this.assemblyContexts = assemblyContexts;
        pcs.firePropertyChange(ASSEMBLY_CONTEXTS_PROPERTY, oldValue, assemblyContexts);
    }

    public int getSimulationTimeMin() {
        return simulationTimeMin;
    }

    public void setSimulationTimeMin(int simulationTimeMin) {
        int oldValue = this.simulationTimeMin;
        this.simulationTimeMin = simulationTimeMin;
        pcs.firePropertyChange(SIMULATION_TIME_MIN_PROPERTY, oldValue, simulationTimeMin);
    }

    public int getSimulationTimeMax() {
        return simulationTimeMax;
    }

    public void setSimulationTimeMax(int simulationTimeMax) {
        int oldValue = this.simulationTimeMax;
        this.simulationTimeMax = simulationTimeMax;
        pcs.firePropertyChange(SIMULATION_TIME_MAX_PROPERTY, oldValue, simulationTimeMax);
    }
    
    public List<TranslatableEntity> getDiagramTypes() {
        return diagramTypes;
    }

    public void setDiagramTypes(List<TranslatableEntity> diagramType) {
        List<TranslatableEntity> oldValue = this.diagramTypes;
        this.diagramTypes = diagramType;
        pcs.firePropertyChange(DIAGRAM_TYPE_PROPERTY, oldValue, diagramType);
    }

    public void clear() {
        setMetrics(null);
        setMeasuringPointsFrom(null);
        setMeasuringPointsTo(null);
        setTriggerTypes(null);
        setTriggerInstances(null);
        setAssemblyContexts(null);
        setSimulationTimeMin(0);
        setSimulationTimeMax(0);
    }

}

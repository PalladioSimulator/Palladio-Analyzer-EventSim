package edu.kit.ipd.sdq.eventsim.rvisualization.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class DiagramModel {

    public static final String DIAGRAM_TYPE_PROPERTY = "diagramType";

    public static final String TITLE_PROPERTY = "title";

    public static final String SUB_TITLE_PROPERTY = "subTitle";

    public static final String SUB_SUB_TITLE_PROPERTY = "subSubTitle";

    public static final String UNBOUND_VARIABLES_PROPERTY = "unboundVariables";

    public static final String VARIABLE_BINDINGS_PROPERTY = "variableBindings";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private DiagramType diagramType;

    private String title;

    private String subTitle;

    private String subSubTitle;

    private List<TranslatableEntity> unboundVariables;

    private List<VariableBinding> variableBindings;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(DiagramType diagramType) {
        DiagramType oldValue = this.diagramType;
        this.diagramType = diagramType;
        pcs.firePropertyChange(DIAGRAM_TYPE_PROPERTY, oldValue, diagramType);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String oldValue = this.title;
        this.title = title;
        pcs.firePropertyChange(TITLE_PROPERTY, oldValue, title);
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        String oldValue = this.subTitle;
        this.subTitle = subTitle;
        pcs.firePropertyChange(SUB_TITLE_PROPERTY, oldValue, subTitle);
    }

    public String getSubSubTitle() {
        return subSubTitle;
    }

    public void setSubSubTitle(String subSubTitle) {
        String oldValue = this.subSubTitle;
        this.subSubTitle = subSubTitle;
        pcs.firePropertyChange(SUB_SUB_TITLE_PROPERTY, oldValue, subSubTitle);
    }

    public List<TranslatableEntity> getUnboundVariables() {
        return unboundVariables;
    }

    public void setUnboundVariables(List<TranslatableEntity> unboundVariables) {
        List<TranslatableEntity> oldValue = this.unboundVariables;
        this.unboundVariables = unboundVariables;
        pcs.firePropertyChange(SUB_SUB_TITLE_PROPERTY, oldValue, unboundVariables);
    }

    public List<VariableBinding> getVariableBindings() {
        return variableBindings;
    }

    public void setVariableBindings(List<VariableBinding> variableBindings) {
        List<VariableBinding> oldValue = this.variableBindings;
        this.variableBindings = variableBindings;
        pcs.firePropertyChange(VARIABLE_BINDINGS_PROPERTY, oldValue, variableBindings);
    }

}

package edu.kit.ipd.sdq.eventsim.rvisualization.model;

public class DiagramModel {

    private DiagramType diagramType;

    private String title;

    private String subTitle;

    private String subSubTitle;

    public DiagramType getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitel) {
        this.subTitle = subTitel;
    }

    public String getSubSubTitle() {
        return subSubTitle;
    }

    public void setSubSubTitle(String subSubTitle) {
        this.subSubTitle = subSubTitle;
    }

}

package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

public enum Aesthetic implements Plottable {

    X("x"), Y("y"), COLOR("color"), LINETYPE("linetype");

    private String representation;

    private Aesthetic(String representation) {
        this.representation = representation;
    }

    @Override
    public String toPlot() {
        return representation;
    }
}

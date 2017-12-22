package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

public enum Aesthetic implements Plottable {

    // TODO not complete yet, compare
    // http://stackoverflow.com/questions/11657380/is-there-a-table-or-catalog-of-aesthetics-for-ggplot2
    X("x"), Y("y"), COLOR("color"), SIZE("size"), LINETYPE("linetype"), ALPHA("alpha"), FILL("fill"), SHAPE(
            "shape"), WIDTH("width"), HEIGHT("width");

    private String representation;

    private Aesthetic(String representation) {
        this.representation = representation;
    }

    @Override
    public String toPlot() {
        return representation;
    }
}

package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

public enum Geom implements Plottable {

    POINT("geom_point"), LINE("geom_line"), HISTOGRAM("geom_histogram"),

    /** TODO actually no geom, but pretty much works like one */
    ECDF("stat_ecdf");

    private String representation;

    private Geom(String representation) {
        this.representation = representation;
    }

    @Override
    public String toPlot() {
        return representation;
    }

    public Layer asLayer() {
        return new LayerImpl(representation);
    }

}

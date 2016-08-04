package edu.kit.ipd.sdq.eventsim.rvisualization.model;

import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Aesthetic;

/**
 * Diagram types for plotting.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public enum DiagramType {

    // TODO available aesthetics need to be refined

    /** */
    HISTOGRAM("Histogram", "Histogram", true, Aesthetic.FILL),

    /** */
    POINT_GRAPH("Point Chart", "Point", false, Aesthetic.COLOR, Aesthetic.LINETYPE, Aesthetic.ALPHA, Aesthetic.FILL, Aesthetic.SHAPE),

    /** */
    CDF("CDF (Cumulative Distribution Function)", "CDF", true, Aesthetic.COLOR),

    /** */
    BAR("Bar Chart", "Bar", false, Aesthetic.COLOR),

    /** */
    LINE("Line Chart", "Line", false, Aesthetic.COLOR, Aesthetic.LINETYPE);

    private String name;

    private String shortName;

    private boolean aggregating;

    private Aesthetic[] aesthetics;

    private DiagramType(String name, String shortName, boolean aggregating, Aesthetic... aesthetics) {
        this.name = name;
        this.shortName = shortName;
        this.aggregating = aggregating;
        this.aesthetics = aesthetics;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * returns {@code true}, if this diagram type aggregates simulation results (like a histogram);
     * {@code false}, if not (like a point chart).
     */
    public boolean isAggregating() {
        return aggregating;
    }

    public Aesthetic[] getAesthetics() {
        return aesthetics;
    }

}

package edu.kit.ipd.sdq.eventsim.rvisualization.model;

/**
 * Diagram types for plotting.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public enum DiagramType {

    /** */
    HISTOGRAM("Histogram", "Histogram", true),

    /** */
    POINT_GRAPH("X/Y Point Graph", "Point Graph", false),

    /** */
    CDF("CDF (Cumulative Distribution Function)", "CDF", true);

    private String name;
    
    private String shortName;

    private boolean aggregating;

    private DiagramType(String name, String shortName, boolean aggregating) {
        this.name = name;
        this.shortName = shortName;
        this.aggregating = aggregating;
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

}

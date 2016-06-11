package edu.kit.ipd.sdq.eventsim.rvisualization.model;

/**
 * Diagram types for plotting.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public enum DiagramType {

    HISTOGRAM("Histogram", true), POINT_GRAPH("X/Y Point Graph", false);

    private String name;

    private boolean aggregating;

    private DiagramType(String name, boolean aggregating) {
        this.name = name;
        this.aggregating = aggregating;
    }

    public String getName() {
        return name;
    }
    
    /**
     * returns {@code true}, if this diagram type aggregates simulation results (like a histogram);
     * {@code false}, if not (like a point chart).
     */
    public boolean isAggregating() {
        return aggregating;
    }

}

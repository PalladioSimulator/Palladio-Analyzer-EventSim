package edu.kit.ipd.sdq.eventsim.rvisualization.model;

/**
 * Diagram types for plotting.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public enum DiagramType {

    HISTOGRAM(true), POINT_GRAPH(false);

    private boolean aggregating;

    private DiagramType(boolean aggregating) {
        this.aggregating = aggregating;
    }

    /**
     * returns {@code true}, if this diagram type aggregates simulation results (like a histogram);
     * {@code false}, if not (like a point chart).
     */
    public boolean isAggregating() {
        return aggregating;
    }

}

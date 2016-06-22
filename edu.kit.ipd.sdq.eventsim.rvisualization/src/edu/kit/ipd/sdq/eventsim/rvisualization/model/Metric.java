//package edu.kit.ipd.sdq.eventsim.rvisualization.model;
//
///**
// * Enumeration of metrics, their individual display name, and their technical name used by
// * EventSim.Measurements.
// * 
// * @author Benjamin Rupp
// * @author Philipp Merkle
// *
// */
//public class Metric {
//
//    /** */
//    HOLD_TIME("Hold Time of Passive Resources", "HOLD_TIME"),
//
//    /** */
//    QUEUE_LENGTH("Queue Length of Resources", "QUEUE_LENGTH"),
//
//    /** */
//    RESOURCE_DEMAND("Resource Demand", "RESOURCE_DEMAND"),
//
//    /** */
//    TIME_SPAN("Response Time (Time Span)", "TIME_SPAN"),
//
//    /** */
//    WAITING_TIME("Waiting Time for Passive Resources", "WAITING_TIME");
//
//    private String name;
//
//    private String nameInMeasurements;
//
//    private Metric(String name, String nameInMeasurements) {
//        this.name = name;
//        this.nameInMeasurements = nameInMeasurements;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getNameInMeasurements() {
//        return nameInMeasurements;
//    }
//
//    public static Metric fromMeasurementsName(String nameInMeasurements) {
//        for (Metric m : Metric.values()) {
//            if (m.getNameInMeasurements().equals(nameInMeasurements)) {
//                return m;
//            }
//        }
//        return null;
//    }
//
//}

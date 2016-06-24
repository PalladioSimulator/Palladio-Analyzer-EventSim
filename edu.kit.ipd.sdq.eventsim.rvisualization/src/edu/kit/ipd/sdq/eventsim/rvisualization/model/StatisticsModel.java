package edu.kit.ipd.sdq.eventsim.rvisualization.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class StatisticsModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private int observations;

    private double min;

    private double firstQuartile;

    private double median;

    private double mean;

    private double thirdQuartile;

    private double max;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public int getObservations() {
        return observations;
    }

    public void setObservations(int observations) {
        int oldValue = this.observations;
        this.observations = observations;
        pcs.firePropertyChange("observations", oldValue, observations);
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        double oldValue = this.min;
        this.min = min;
        pcs.firePropertyChange("min", oldValue, min);
    }

    public double getFirstQuartile() {
        return firstQuartile;
    }

    public void setFirstQuartile(double firstQuartile) {
        double oldValue = this.firstQuartile;
        this.firstQuartile = firstQuartile;
        pcs.firePropertyChange("firstQuartile", oldValue, firstQuartile);
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        double oldValue = this.median;
        this.median = median;
        pcs.firePropertyChange("median", oldValue, median);
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        double oldValue = this.mean;
        this.mean = mean;
        pcs.firePropertyChange("mean", oldValue, mean);
    }

    public double getThirdQuartile() {
        return thirdQuartile;
    }

    public void setThirdQuartile(double thirdQuartile) {
        double oldValue = this.thirdQuartile;
        this.thirdQuartile = thirdQuartile;
        pcs.firePropertyChange("thirdQuartile", oldValue, thirdQuartile);
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        double oldValue = this.max;
        this.max = max;
        pcs.firePropertyChange("max", oldValue, max);
    }

}

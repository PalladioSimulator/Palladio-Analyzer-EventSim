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
    
    private double quantile1;
    
    private double quantile2;
    
    private double quantile3;
    
    private double quantile4;
    
    private double quantile5;
    
    private double quantile6;
    
    private double quantile7;
    
    private double quantile8;
    
    private double quantile9;

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

    public double getQuantile1() {
        return quantile1;
    }

    public void setQuantile1(double quantile1) {
        double oldValue = this.quantile1;
        this.quantile1 = quantile1;
        pcs.firePropertyChange("quantile1", oldValue, quantile1);
    }

    public double getQuantile2() {
        return quantile2;
    }

    public void setQuantile2(double quantile2) {
        double oldValue = this.quantile2;
        this.quantile2 = quantile2;
        pcs.firePropertyChange("quantile2", oldValue, quantile2);
    }

    public double getQuantile3() {
        return quantile3;
    }

    public void setQuantile3(double quantile3) {
        double oldValue = this.quantile3;
        this.quantile3 = quantile3;
        pcs.firePropertyChange("quantile3", oldValue, quantile3);
    }

    public double getQuantile4() {
        return quantile4;
    }

    public void setQuantile4(double quantile4) {
        double oldValue = this.quantile4;
        this.quantile4 = quantile4;
        pcs.firePropertyChange("quantile4", oldValue, quantile4);
    }

    public double getQuantile5() {
        return quantile5;
    }

    public void setQuantile5(double quantile5) {
        double oldValue = this.quantile5;
        this.quantile5 = quantile5;
        pcs.firePropertyChange("quantile5", oldValue, quantile5);
    }

    public double getQuantile6() {
        return quantile6;
    }

    public void setQuantile6(double quantile6) {
        double oldValue = this.quantile6;
        this.quantile6 = quantile6;
        pcs.firePropertyChange("quantile6", oldValue, quantile6);
    }

    public double getQuantile7() {
        return quantile7;
    }

    public void setQuantile7(double quantile7) {
        double oldValue = this.quantile7;
        this.quantile7 = quantile7;
        pcs.firePropertyChange("quantile7", oldValue, quantile7);
    }

    public double getQuantile8() {
        return quantile8;
    }

    public void setQuantile8(double quantile8) {
        double oldValue = this.quantile8;
        this.quantile8 = quantile8;
        pcs.firePropertyChange("quantile8", oldValue, quantile8);
    }

    public double getQuantile9() {
        return quantile9;
    }

    public void setQuantile9(double quantile9) {
        double oldValue = this.quantile9;
        this.quantile9 = quantile9;
        pcs.firePropertyChange("quantile9", oldValue, quantile9);
    }


    
}

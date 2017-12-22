package edu.kit.ipd.sdq.eventsim.resources.rjobs.window;

import edu.kit.ipd.sdq.eventsim.measurement.r.window.WindowCalculator;

public class MeanQueueLengthCalculator implements WindowCalculator {

    private double windowSize;

    public MeanQueueLengthCalculator(double windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public double processValue(double queueLength, double duration) {
        return (queueLength * duration) / windowSize;
    }

}

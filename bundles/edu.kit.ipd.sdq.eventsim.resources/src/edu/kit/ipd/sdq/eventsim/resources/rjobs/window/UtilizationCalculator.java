package edu.kit.ipd.sdq.eventsim.resources.rjobs.window;

import edu.kit.ipd.sdq.eventsim.measurement.r.window.WindowCalculator;

public class UtilizationCalculator implements WindowCalculator {

    private double windowSize;

    public UtilizationCalculator(double windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public double processValue(double queueLength, double consumedDuration) {
        if (queueLength > 0) { // busy
            double busyDuration = consumedDuration;
            return busyDuration / windowSize;
        } else if (queueLength == 0) { // idle
            return 0;
        } else { // negative queue length
            throw new RuntimeException("Encountered negative queue length: " + queueLength);
        }
    }

}

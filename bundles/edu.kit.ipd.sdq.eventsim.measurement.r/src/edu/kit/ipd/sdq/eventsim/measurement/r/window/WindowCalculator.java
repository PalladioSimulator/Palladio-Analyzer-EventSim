package edu.kit.ipd.sdq.eventsim.measurement.r.window;

public interface WindowCalculator {

    /**
     * Calculates how the current window's value has to change when integrating the given
     * observation:
     * 
     * <pre>
     * currentWindow = currentWindow + processValue(observation, duration)
     * </pre>
     * 
     * This method will be called at least once per window. The common case is, however, that this
     * method is called multiple times for each window in order to incrementally fill the window.
     * 
     * @param observation
     *            the observation to be integrated
     * @param duration
     *            the time span the observation is valid
     * @return the summand that needs to be added (mathematically) to the current window's value so
     *         that the observations subsumed by the window include the passed observation.
     */
    double processValue(double observation, double duration);

}

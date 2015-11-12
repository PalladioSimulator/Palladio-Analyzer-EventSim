package edu.kit.ipd.sdq.eventsim.measurement.r;

public class RStatistics {

	private double timeSpentInR; // in milliseconds

	public void captureTimeSpentInR(double millisecondsSpentInR) {
		timeSpentInR += millisecondsSpentInR;
	}

	public double getTotalTimeSpentInR() {
		return timeSpentInR;
	}

	public void reset() {
		timeSpentInR = 0;
	}

}
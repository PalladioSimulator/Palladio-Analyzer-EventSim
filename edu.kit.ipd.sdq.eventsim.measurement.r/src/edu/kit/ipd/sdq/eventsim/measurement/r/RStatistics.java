package edu.kit.ipd.sdq.eventsim.measurement.r;

public class RStatistics {

	private long timeSpentInR; // in milliseconds

	public void captureTimeSpentInR(long millisecondsSpentInR) {
		timeSpentInR += millisecondsSpentInR;
	}

	public long getTotalTimeSpentInR() {
		return timeSpentInR;
	}

	public void reset() {
		timeSpentInR = 0;
	}

}
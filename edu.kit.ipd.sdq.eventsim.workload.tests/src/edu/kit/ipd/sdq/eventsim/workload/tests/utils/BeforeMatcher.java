package edu.kit.ipd.sdq.eventsim.workload.tests.utils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import edu.kit.ipd.sdq.eventsim.workload.tests.utils.Tracer.TracedMeasurement;

public class BeforeMatcher extends TypeSafeMatcher<Tracer.TracedMeasurement> {

	private TracedMeasurement secondMeasurement;

	public BeforeMatcher(TracedMeasurement measurement) {
		this.secondMeasurement = measurement;
	}

	@Override
	protected boolean matchesSafely(TracedMeasurement firstMeasurement) {
		return firstMeasurement.getSequenceNumber() < secondMeasurement.getSequenceNumber();
	}

	@Override
	public void describeTo(Description description) {
		// TODO improve description
		description.appendText("Sequence number lower than " + secondMeasurement.getSequenceNumber());
	}

	public static BeforeMatcher before(TracedMeasurement measurement) {
		return new BeforeMatcher(measurement);
	}

}

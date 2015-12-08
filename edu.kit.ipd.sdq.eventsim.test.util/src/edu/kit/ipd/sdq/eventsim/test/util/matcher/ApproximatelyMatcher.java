package edu.kit.ipd.sdq.eventsim.test.util.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ApproximatelyMatcher extends TypeSafeMatcher<Integer> {

	private final Integer number;

	private final Integer tolerance;

	public ApproximatelyMatcher(Integer number, Integer tolerance) {
		this.number = number;
		this.tolerance = tolerance;
	}

	@Override
	protected boolean matchesSafely(Integer number) {
		return Math.abs(this.number - number) < tolerance;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Value between " + (number - tolerance) + " and " + (number + tolerance));
	}

	public static ApproximatelyMatcher approximately(Integer number, Integer tolerance) {
		return new ApproximatelyMatcher(number, tolerance);
	}

}

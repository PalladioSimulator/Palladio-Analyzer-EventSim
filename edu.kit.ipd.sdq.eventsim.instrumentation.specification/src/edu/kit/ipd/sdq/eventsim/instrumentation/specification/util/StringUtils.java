package edu.kit.ipd.sdq.eventsim.instrumentation.specification.util;

public class StringUtils {

	private static final String[] SIBILANTS = { "s", "x", "ch" };
	private static final String[] VOWALS = { "a", "e", "i", "o", "u" };

	private StringUtils() {
	}

	public static String pluralize(String singular) {
		if (singular == null) {
			throw new IllegalArgumentException("The singular cannot be null");
		}

		for (String sibilant : SIBILANTS) {
			if (singular.endsWith(sibilant)) {
				return singular + "es";
			}
		}

		if (singular.endsWith("y")) {
			boolean endsWithVowal = false;
			for (String vowal : VOWALS) {
				if (singular.substring(0, singular.length() - 1).endsWith(vowal)) {
					endsWithVowal = true;
				}
			}

			if (!endsWithVowal) {
				return singular.substring(0, singular.length() - 1) + "ies";
			}
		}

		return singular + "s";
	}

}

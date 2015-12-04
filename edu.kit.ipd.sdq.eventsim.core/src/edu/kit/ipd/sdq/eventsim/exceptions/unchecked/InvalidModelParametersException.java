package edu.kit.ipd.sdq.eventsim.exceptions.unchecked;

import edu.kit.ipd.sdq.eventsim.exceptions.UncheckedSimulationException;

public class InvalidModelParametersException extends UncheckedSimulationException {

	private static final long serialVersionUID = -5831810659656941182L;

	public InvalidModelParametersException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidModelParametersException(String message) {
		super(message);
	}

	public InvalidModelParametersException(Throwable cause) {
		super(cause);
	}
	
}

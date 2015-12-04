package edu.kit.ipd.sdq.eventsim.exceptions.unchecked;

import edu.kit.ipd.sdq.eventsim.exceptions.UncheckedSimulationException;

public class UnknownSimulationException extends UncheckedSimulationException{

	private static final long serialVersionUID = -1619806017513421175L;

	public UnknownSimulationException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownSimulationException(String message) {
		super(message);
	}

	public UnknownSimulationException(Throwable cause) {
		super(cause);
	}	
	
}

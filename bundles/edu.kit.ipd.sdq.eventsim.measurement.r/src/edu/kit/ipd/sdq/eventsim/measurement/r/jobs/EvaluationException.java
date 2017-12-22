package edu.kit.ipd.sdq.eventsim.measurement.r.jobs;

/**
 * Indicates that the evaluation of one or more R statements via Rserve went wrong.
 * 
 * @author Philipp Merkle
 *
 */
public class EvaluationException extends Exception {

	private static final long serialVersionUID = 1L;

	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
	}

	public EvaluationException(String message) {
		super(message);
	}

	public EvaluationException(Throwable cause) {
		super(cause);
	}

}

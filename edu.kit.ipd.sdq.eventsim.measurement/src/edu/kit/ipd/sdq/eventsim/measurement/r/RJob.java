package edu.kit.ipd.sdq.eventsim.measurement.r;

public interface RJob {

	/**
	 * The work to be done by this job. Should only be called by an {@link RJobProcessor}.
	 */
	void process(RContext context);

	/**
	 * @return the name of this job
	 */
	String getName();

}
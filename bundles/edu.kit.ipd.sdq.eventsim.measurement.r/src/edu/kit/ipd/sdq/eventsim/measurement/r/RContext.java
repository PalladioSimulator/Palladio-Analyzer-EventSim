package edu.kit.ipd.sdq.eventsim.measurement.r;

import org.rosuda.REngine.Rserve.RConnection;

public class RContext {

	private RConnection connection;
	
	private RStatistics statistics;
	
	public RContext(RConnection connection) {
		this.connection = connection;
		this.statistics = new RStatistics();
	}
	
	public RConnection getConnection() {
		return connection;
	}
	
	public RStatistics getStatistics() {
		return statistics;
	}
	
}

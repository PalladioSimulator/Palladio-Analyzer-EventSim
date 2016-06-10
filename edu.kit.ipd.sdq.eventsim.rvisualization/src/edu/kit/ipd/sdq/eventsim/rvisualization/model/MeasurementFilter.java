package edu.kit.ipd.sdq.eventsim.rvisualization.model;

/**
 * Filter condition for filtering R data.
 * 
 * @author Benjamin Rupp
 *
 */
public class MeasurementFilter {

	/**
	 * Filter property (e.g. where.first.type).
	 */
	private String filterProperty;

	/**
	 * Filter operator (e.g. '==').
	 */
	private String filterOperator;
	
	private String condition;

	/**
	 * Create a new filter.
	 * 
	 * @param property
	 *            Filter property ({@link #filterProperty}).
	 * @param operator
	 *            Filter operator ({@link #filterOperator}).
	 */
	public MeasurementFilter(final String property, final String operator, final String condition) {
		this.filterProperty = property;
		this.filterOperator = operator;
		this.condition = "'" + condition + "'";
	}
	
	public MeasurementFilter(final String property, final String operator, final int condition) {
		this.filterProperty = property;
		this.filterOperator = operator;
		this.condition = Integer.toString(condition);
	}

	/**
	 * Get filter property.
	 * 
	 * @return Filter property.
	 */
	public final String getProperty() {
		return this.filterProperty;
	}

	/**
	 * Get filter operator.
	 * 
	 * @return Filter operator.
	 */
	public final String getOperator() {
		return this.filterOperator;
	}

	/**
	 * Get filter condition.
	 * 
	 * @return Filter condition.
	 */
	public final String getCondition() {
		return condition;
	}
	
}

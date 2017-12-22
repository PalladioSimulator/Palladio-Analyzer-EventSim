package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a calculator. A calculator can be set on top of two probes to
 * compute a metric based on the measurement results of the probes.
 * 
 * @author Henning Schulz
 */
@XmlRootElement(name = "calculator")
public class CalculatorRepresentative {

	private ProbeRepresentative fromProbe;
	private ProbeRepresentative toProbe;

	private String metric;

	public CalculatorRepresentative(String metric, ProbeRepresentative fromProbe, ProbeRepresentative toProbe) {
		this.metric = metric;
		this.fromProbe = fromProbe;
		this.toProbe = toProbe;
	}

	public CalculatorRepresentative(String metric) {
		this(metric, null, null);
	}

	public CalculatorRepresentative() {
	}

	@XmlElement(name = "metric")
	public String getMetric() {
		return metric;
	}

	public void setMetric(String measuredProperty) {
		this.metric = measuredProperty;
	}

	@XmlElement(name = "from")
	public ProbeRepresentative getFromProbe() {
		return fromProbe;
	}

	public void setFromProbe(ProbeRepresentative fromProbe) {
		this.fromProbe = fromProbe;
	}

	@XmlElement(name = "to")
	public ProbeRepresentative getToProbe() {
		return toProbe;
	}

	public void setToProbe(ProbeRepresentative toProbe) {
		this.toProbe = toProbe;
	}

	public boolean uses(ProbeRepresentative probe) {
		return fromProbe.equals(probe) || toProbe.equals(probe);
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;

		result = prime * result + ((metric == null) ? 0 : metric.hashCode());
		result = prime * result + ((fromProbe == null) ? 0 : fromProbe.hashCode());
		result = prime * result + ((toProbe == null) ? 0 : toProbe.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		CalculatorRepresentative other = (CalculatorRepresentative) obj;
		if (metric == null) {
			if (other.metric != null)
				return false;
		} else if (!metric.equals(other.metric))
			return false;
		if (fromProbe == null) {
			if (other.fromProbe != null)
				return false;
		} else if (!fromProbe.equals(other.fromProbe))
			return false;
		if (toProbe == null) {
			if (other.toProbe != null)
				return false;
		} else if (!toProbe.equals(other.toProbe))
			return false;
		return true;
	}

}

package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a probe which is capable to measure a certain property for a
 * certain type.
 * 
 * @author Henning Schulz
 *
 * @param <M>
 *            The type of the element to be probed, meaning this probe can be
 *            applied to all elements of a type extending {@code M}.
 */
@XmlRootElement(name = "probe")
public class ProbeRepresentative {

	private Class<?> probedType;
	private String measuredProperty;

	public ProbeRepresentative(String measuredProperty, Class<?> probedType) {
		this.measuredProperty = measuredProperty;
		this.probedType = probedType;
	}

	public ProbeRepresentative() {
	}

	@XmlElement(name = "probed-type")
	public Class<?> getProbedType() {
		return probedType;
	}

	public void setProbedType(Class<?> typeUnderMeasurement) {
		this.probedType = typeUnderMeasurement;
	}

	@XmlElement(name = "property")
	public String getMeasuredProperty() {
		return measuredProperty;
	}

	public void setMeasuredProperty(String measuredProperty) {
		this.measuredProperty = measuredProperty;
	}

	public boolean isApplicableTo(Class<?> type) {
		return probedType.isAssignableFrom(type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((probedType == null) ? 0 : probedType.hashCode());
		result = prime * result + ((measuredProperty == null) ? 0 : measuredProperty.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProbeRepresentative other = (ProbeRepresentative) obj;
		if (probedType == null) {
			if (other.probedType != null)
				return false;
		} else if (!probedType.equals(other.probedType))
			return false;
		if (measuredProperty == null) {
			if (other.measuredProperty != null)
				return false;
		} else if (!measuredProperty.equals(other.measuredProperty))
			return false;
		return true;
	}

}
